package ninja.natsuko.bot;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.ini4j.Wini;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.commands.Command;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.moderation.ModLogger;
import ninja.natsuko.bot.scriptengine.ScriptRunner;
import ninja.natsuko.bot.util.Utilities;

public class Main {
	public static Map<String, Command> commands = new HashMap<>();
	public static MongoDatabase db;
	public static DiscordClient client;
	public static Map<Snowflake,ScriptRunner> modengine = new HashMap<>();
	public static Thread timedEventThread;
	public static Thread uniqueResetThread;
	public static Thread messageResetThread;
	public static Thread resetAntispam;

	
	static String inst = "null";
	static Map<Snowflake,Map<Snowflake,Integer>> uniqueServersJoined = new HashMap<>();
	static Map<Snowflake,Integer> messagesLastSecond = new HashMap<>();
	static Map<Snowflake,Integer> exceededAntispamLimit = new HashMap<>();
	
	public static void main(String[] args) {
		Logger root = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		uniqueResetThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(86400000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					uniqueServersJoined.clear();
				}
			}
		});
		uniqueResetThread.start();
		messageResetThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(2500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					messagesLastSecond.clear();
				}
			}
		});
		messageResetThread.start();
		resetAntispam = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					exceededAntispamLimit.clear();
				}
			}
		});
		resetAntispam.start();
		timedEventThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						//dont care because interrupt means process timed e's immediately
					}
					if(!client.isConnected()) continue; //client isnt ready yet dont jump the shit
					for(Document i : db.getCollection("timed").find(Document.parse("{\"due\":{\"$lte\":"+Instant.now().toEpochMilli()+"}}"))) {
						Map<String,Object> opts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(client.getGuildById(Snowflake.of(i.getLong("guild"))).block())).first().get("options", new HashMap<>());
						try {
							switch(i.getString("type")) {
							case "unban":
								client.getGuildById(Snowflake.of(i.getLong("guild"))).block().unban(Snowflake.of(i.getString("target")), "Natsuko auto-unban after "+Instant.now().minusMillis(i.getLong("due")).toEpochMilli()+"ms").subscribe();
								ModLogger.logCase(client.getGuildById(Snowflake.of(i.getLong("guild"))).block(), ModLogger.newCase(client.getUserById(Snowflake.of(i.getString("target"))).block(), client.getSelf().block(), "Natsuko auto-unban after "+Instant.now().minusMillis(i.getLong("due")).toEpochMilli()+"ms", null, CaseType.UNBAN, 0, client.getGuildById(Snowflake.of(i.getLong("guild"))).block()));
								db.getCollection("timed").deleteOne(i);
								break;
							case "unmute":
								long roleId = 0l;
								if(!opts.containsKey("mutedrole")) {root.info("Mutedrole didnt exist upon auto-unmute"); return;} //wtf?
								roleId = Long.parseLong(opts.get("mutedrole").toString());
								client.getGuildById(Snowflake.of(i.getLong("guild"))).block().getMemberById(Snowflake.of(i.getString("target"))).block().removeRole(
										Snowflake.of(roleId), "Natsuko auto-unmute after "+Instant.now().minusMillis(i.getLong("due")).toEpochMilli()+"ms").subscribe();
								ModLogger.logCase(client.getGuildById(Snowflake.of(i.getLong("guild"))).block(), ModLogger.newCase(client.getUserById(Snowflake.of(i.getString("target"))).block(), client.getSelf().block(), "Natsuko auto-unmute after "+Instant.now().minusMillis(i.getLong("due")).toEpochMilli()+"ms", null, CaseType.UNMUTE, 0, client.getGuildById(Snowflake.of(i.getLong("guild"))).block()));
								db.getCollection("timed").deleteOne(i);
								root.info("Unmuted a user", i);
								break;
							case "unstrike":
								break;
							default:
								break;
							}
						} catch(Exception e) {
							db.getCollection("timed").deleteOne(i); //it failed to remove it so it doesnt fuck up more things
							root.error("error in timedthread",e);
						}
					}
				}
			}
			
		});
		timedEventThread.start();
		Reflections reflections = new Reflections("ninja.natsuko.bot.commands"); // restrict to command package to prevent unnecessary searching
		Set<Class<? extends Command>> commandClasses = reflections.getSubTypesOf(Command.class);
		commandClasses.forEach((cmd) -> {
			try {
				Command cmdClass = cmd.newInstance();
				commands.put(cmdClass.commandName, cmdClass);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
		
		inst = Double.toString(Math.random());
		try {
			Wini config = new Wini(new File("./config.ini"));
			
			db = MongoClients.create(config.get("Mongo", "uri")).getDatabase(config.get("Mongo", "database"));
			
			DiscordClientBuilder builder = new DiscordClientBuilder(config.get("Config", "token"));
			client = builder.build();	
			client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {
				((MessageChannel)event.getClient().getChannelById(Snowflake.of(592781286297305091l)).block()).createMessage("n;kill "+inst).subscribe();
				event.getClient().updatePresence(Presence.online(Activity.playing("Natsuko | "+event.getGuilds().size()+" servers | n;help")));
				return;
			});	
			client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(event->{
				if(event.getGuild().getJoinTime().get().isAfter(Instant.now().minusMillis(1000l))) {
					Main.db.getCollection("guilds").insertOne(Utilities.initGuild(event.getGuild()));
					//TODO fuck botfarms
				}
			});
			client.getEventDispatcher().on(MemberJoinEvent.class).subscribe(event->{
				if(event.getMember().getUsername().matches("(discord.gg|twitter.com|discordapp.com|dis.gd)")) {
					event.getMember().ban(a->{a.setReason("Natsuko autoban for: BadURL in username");a.setDeleteMessageDays(1);});
					ModLogger.logCase(event.getGuild().block(), ModLogger.newCase(event.getMember(), client.getSelf().block(), "Natsuko auto-ban for BadURL in username.", null, CaseType.BAN, 0, event.getGuild().block()));
					Utilities.sendMessage((TextChannel)client.getChannelById(Snowflake.of(591729986465955866l)).block(), "NOTICE: User with BadURL in name: "+event.getMember().getId().asString()+" "+event.getMember().getUsername());
				}
				Map<Snowflake, Integer> temp = uniqueServersJoined.getOrDefault(event.getMember().getId(), new HashMap<>());
				temp.put(event.getGuildId(), temp.getOrDefault(event.getGuildId(), 0)+1);
				uniqueServersJoined.put(event.getMember().getId(), temp);
				if(temp.size()==10||temp.size()%20==0) {
					Utilities.sendMessage((TextChannel)client.getChannelById(Snowflake.of(591729986465955866l)).block(), "NOTICE: User with suspicious behavior: "+event.getMember().getId().asString()+" Has joined "+temp.size()+" unique servers in the last 24 hours.");
				}
				if(temp.get(event.getGuildId())==5||temp.get(event.getGuildId())%10==0) {
					Utilities.sendMessage((TextChannel)client.getChannelById(Snowflake.of(591729986465955866l)).block(), "NOTICE: User with suspicious behavior: "+event.getMember().getId().asString()+" Has joined a server ("+event.getGuildId().asString()+") "+temp.get(event.getGuildId())+" times in the last 24 hours.");
				}
			});
			client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(Main::processCommand);
			client.login().block();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void processCommand(MessageCreateEvent event) {
		try {
			if(!event.getMember().isPresent()) return;
			
			Map<String,Object> opts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(event.getGuild().block())).first().get("options", new HashMap<>());
			if(messagesLastSecond.getOrDefault(event.getMember().get().getId(),0)>=3) {
				if(opts.getOrDefault("automod.antispam","on").toString().equals("on")) {
					event.getMessage().delete().subscribe();
					exceededAntispamLimit.put(event.getMember().get().getId(),exceededAntispamLimit.getOrDefault(event.getMember().get().getId(),0)+1);
					if(exceededAntispamLimit.get(event.getMember().get().getId())>3) {
						Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(event.getGuild().block())).first();
						List<Document> strikes = guildoc.get("strikes", new ArrayList<>());
						List<Document> temp = strikes.stream().filter(a->a.getLong("id") == event.getMember().get().getId().asLong()).collect(Collectors.toList());
						Document userStrikes;
						if(temp.size() < 1) {
							userStrikes = Document.parse("{\"id\":"+event.getMember().get().getId().asLong()+",\"strikes\":1}");
							strikes.add(userStrikes);
						} else {
							userStrikes = temp.get(0);
							int i = strikes.indexOf(userStrikes);
							userStrikes.put("strikes", userStrikes.getInteger("strikes")+1);
							strikes.set(i, userStrikes);
						}
						guildoc.put("strikes", strikes);
						Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(event.getGuild().block()), guildoc);
						Utilities.processStrike(event.getMember().get(),userStrikes.getInteger("strikes"));
						ModLogger.logCase(event.getGuild().block(), ModLogger.newCase(event.getMember().get(), event.getMember().get(), "Natsuko auto-strike for antispam", null, CaseType.STRIKE, 1, event.getGuild().block()));
					}
					return;
				}
			}
			messagesLastSecond.put(event.getMember().get().getId(), messagesLastSecond.getOrDefault(event.getMember().get().getId(),0)+1);
			
			String msg;
			if (event.getMessage().getContent().isPresent()) {
				msg = event.getMessage().getContent().get();
			} else {
				return;
			}
			
			/*if(opts.getOrDefault("automod.anticopypasta","on").toString().equals("on")) {
				if(event.getMessage().getContent().get().matches("")) {
					
				}
			}*/ //disabled for now, no database of copypastaes
			
			// commands area
			
			String[] vomit;

			try {
				vomit = msg.substring(2).split(" ");
			} catch (ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException e) {
				// ignore and skip
				return;
			}
			
			String cmd = vomit[0];
			
			if(cmd.equalsIgnoreCase("kill") && event.getMember().get().getId().asLong() == event.getClient().getSelfId().get().asLong()) {
				if(!msg.contains(inst))System.exit(0);
			}
			
			if(event.getMember().get().isBot()) return;
			
			if (!msg.startsWith("n;")) {
				if(!modengine.containsKey(event.getGuild().block().getId())) {
					modengine.put(event.getGuild().block().getId(),new ScriptRunner(event.getGuild().block()));
				}
				modengine.get(event.getGuild().block().getId()).run(event.getMessage());
				return;
			}
			
			String[] args = Arrays.stream(vomit).skip(1).toArray(String[]::new);

			if (commands.get(cmd) != null) 
				commands.get(cmd).execute(args, event);
			
			//command execution takes execution precedence over modengine
			if(!modengine.containsKey(event.getGuild().block().getId())) {
				modengine.put(event.getGuild().block().getId(),new ScriptRunner(event.getGuild().block()));
			}
			modengine.get(event.getGuild().block().getId()).run(event.getMessage());
			return;
		} catch(Exception e) {
			StringWriter string = new StringWriter();
			PrintWriter print = new PrintWriter(string);
			e.printStackTrace(print);
			String trace = string.toString();
			event.getMessage().getChannel().block().createMessage(":warning: An error has occurred!\n```"+trace.substring(0,Math.min(1900, trace.length()))+"```").subscribe();
			e.printStackTrace();
		}
	}
}