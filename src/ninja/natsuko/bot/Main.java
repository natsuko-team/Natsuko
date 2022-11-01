package ninja.natsuko.bot;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bson.Document;
import org.ini4j.Wini;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.rest.RestClient;
import discord4j.rest.entity.RestGuild;
import discord4j.rest.http.client.ClientException;
import ninja.natsuko.bot.commands.Command;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.moderation.ModLogger;
import ninja.natsuko.bot.scriptengine.ScriptRunner;
import ninja.natsuko.bot.util.ArgumentParser;
import ninja.natsuko.bot.util.ErrorHandler;
import ninja.natsuko.bot.util.Utilities;

public class Main {
	public static Map<String, Command> commands = new HashMap<>();
	public static MongoDatabase db;
	public static GatewayDiscordClient client;
	public static Map<Snowflake,ScriptRunner> modengine = new HashMap<>();
	public static Thread timedEventThread;
	public static Thread uniqueResetThread;
	public static Thread messageResetThread;
	public static Thread resetAntispam;

	private static final String guildString = "guild";
	private static final String guildsString = "guilds";
	private static final String timedString = "timed";
	private static final String strikesString = "strikes";
	private static final String natsukoPipeString = "Natsuko | ";
	private static final String serversNPipeSemicolonHelpString = " servers | n;help";
	private static final String blockopenQuoteIdQuoteColonString = "{\"id\":";
	private static final String cachetString = "Cachet";
	
	
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
						Thread.currentThread().interrupt();
						root.error("Uniques Reset Interrupted", e);
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
						Thread.currentThread().interrupt();
						root.error("MLS Reset Interrupted", e);
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
						Thread.currentThread().interrupt();
						root.error("Antispam Exceeded Reset Interrupted", e);
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
						Thread.currentThread().interrupt();
						root.error("Timed Events Interrupted", e);
					}
					for(Document i : db.getCollection(timedString).find(Document.parse("{\"due\":{\"$lte\":"+Instant.now().toEpochMilli()+"}}"))) {
						RestClient restclient = client.getRestClient();
						Map<String,Object> opts = Main.db.getCollection(guildsString).find(Utilities.guildToFindDoc(client.getGuildById(Snowflake.of(i.getLong(guildString))).block())).first().get("options", new HashMap<>());
						try {
							switch(i.getString("type")) {
							case "unban":
								restclient.getGuildById(Snowflake.of(i.getLong(guildString))).removeGuildBan(Snowflake.of(i.getString("target")), "Natsuko auto-unban after "+Instant.now().minusMillis(i.getLong("due")).toEpochMilli()+"ms").subscribe();
								ModLogger.logCase(client.getGuildById(Snowflake.of(i.getLong(guildString))).block(), ModLogger.newCase(client.getUserById(Snowflake.of(i.getString("target"))).block(), client.getSelf().block(), "Natsuko auto-unban after "+Instant.now().minusMillis(i.getLong("due")).toEpochMilli()+"ms", null, CaseType.UNBAN, 0, client.getGuildById(Snowflake.of(i.getLong(guildString))).block()));
								db.getCollection(timedString).deleteOne(i);
								break;
							case "unmute":
								long roleId = 0l;
								if(!opts.containsKey("mutedrole")) {root.info("Mutedrole didnt exist upon auto-unmute"); return;} //wtf?
								roleId = Long.parseLong(opts.get("mutedrole").toString());
								restclient.getGuildById(Snowflake.of(i.getLong(guildString))).removeMemberRole(Snowflake.of(i.getString("target")),
										Snowflake.of(roleId), "Natsuko auto-unmute after "+Instant.now().minusMillis(i.getLong("due")).toEpochMilli()+"ms").subscribe();
								ModLogger.logCase(client.getGuildById(Snowflake.of(i.getLong(guildString))).block(), ModLogger.newCase(client.getUserById(Snowflake.of(i.getString("target"))).block(), client.getSelf().block(), "Natsuko auto-unmute after "+Instant.now().minusMillis(i.getLong("due")).toEpochMilli()+"ms", null, CaseType.UNMUTE, 0, client.getGuildById(Snowflake.of(i.getLong(guildString))).block()));
								db.getCollection(timedString).deleteOne(i);
								root.info("Unmuted a user", i);
								break;
							case "unstrike":
								Guild guild = client.getGuildById(Snowflake.of(i.getLong(guildString))).block();
								Document guildoc = Main.db.getCollection(guildsString).find(Utilities.guildToFindDoc(guild)).first();
								List<Document> strikes = guildoc.get(strikesString, new ArrayList<>());
								List<Document> temp = strikes.stream().filter(a->a.getLong("id") == Snowflake.of(i.getString("target")).asLong()).collect(Collectors.toList());
								Document userStrikes;
								if(temp.size() < 1) {
									userStrikes = Document.parse(blockopenQuoteIdQuoteColonString+i.getString("target")+",\"strikes\":0}");
								} 
								else
									userStrikes = temp.get(0);
								if(userStrikes == null) {
									userStrikes = Document.parse(blockopenQuoteIdQuoteColonString+i.getString("target")+",\"strikes\":0}");
									strikes.add(userStrikes);
								}
								else {
									int j = strikes.indexOf(userStrikes);
									userStrikes.put(strikesString, userStrikes.getInteger(strikesString)-1);
									strikes.set(j, userStrikes);
								}
								guildoc.put(strikesString, strikes);
								Main.db.getCollection(guildsString).replaceOne(Utilities.guildToFindDoc(guild), guildoc);
								ModLogger.logCase(client.getGuildById(Snowflake.of(i.getLong(guildString))).block(), ModLogger.newCase(client.getUserById(Snowflake.of(i.getString("target"))).block(), client.getSelf().block(), "Natsuko auto-unstrike after "+Instant.now().minusMillis(i.getLong("due")).toEpochMilli()+"ms", null, CaseType.UNSTRIKE, 1, client.getGuildById(Snowflake.of(i.getLong(guildString))).block()));
								break;
							default:
								break;
							}
						} catch(Exception e) {
							db.getCollection(timedString).deleteOne(i); //it failed to remove it so it doesnt fuck up more things
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
			
			client = DiscordClientBuilder.create(config.get("Config", "token")).build().login().block();
			client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {
				((MessageChannel)event.getClient().getChannelById(Snowflake.of(592781286297305091l)).block()).createMessage("n;kill "+inst).subscribe();
				event.getClient().updatePresence(Presence.online(Activity.playing(natsukoPipeString+event.getGuilds().size()+serversNPipeSemicolonHelpString))).block();
				return;
			});	
			client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(event->{
				event.getClient().updatePresence(Presence.online(Activity.playing(natsukoPipeString+event.getClient().getGuilds().count().block()+serversNPipeSemicolonHelpString))).block();
				if(event.getGuild().getJoinTime().isAfter(Instant.now().minusMillis(1000l))) {
					
					Main.db.getCollection(guildsString).insertOne(Utilities.initGuild(event.getGuild()));
					//TODO fuck botfarms
				}
			});
			client.getEventDispatcher().on(GuildDeleteEvent.class).subscribe(event->{
				event.getClient().updatePresence(Presence.online(Activity.playing(natsukoPipeString+event.getClient().getGuilds().count().block()+serversNPipeSemicolonHelpString))).block();
			});
			client.getEventDispatcher().on(MemberJoinEvent.class).subscribe(event->{
				if(event.getMember().getUsername().matches("(discord.gg|twitter.com|discordapp.com|dis.gd|discord.com)")) {
					try {
					event.getMember().ban(a->{a.setReason("Natsuko autoban for: BadURL in username");a.setDeleteMessageDays(1);}).block();
					} catch (ClientException e) {return;/*probs missing perms*/}
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
			new Thread(() -> {
				URL url;
				try {
					url = new URL(config.get(cachetString, "url"));
				} catch (MalformedURLException e) {
					root.error("Status URL malformed. Not bothering to run status thread.");
					return;
				}
				
				String metricId = config.get(cachetString, "metric");
				String apiKey = config.get(cachetString, "key");
				
				String metricsUrl;
				try {
					metricsUrl = new URL(url, "/api/v1/metrics/" + metricId + "/points").toString();
				} catch (MalformedURLException e1) {
					root.error("URL is wrong!");
					return;
				}
				root.info("Metrics URL: " + metricsUrl);
				
				while (true) {
					try {
						Thread.sleep(15000);

						String data = "{\"value\":" + client.getGatewayClient(0).map((a)->a.getResponseTime().toMillis()).get() + "}";
						
						try (CloseableHttpClient client1 = HttpClients.createDefault()) {
							HttpPost post = new HttpPost(metricsUrl);
							
							post.setEntity(new StringEntity(data));
							
							post.setHeader("X-Cachet-Token", apiKey);
							post.setHeader("Accept", "application/json");
							post.setHeader("Content-type", "application/json");
							
							client1.execute(post).close(); // close the response, not the client
						}
					} catch (InterruptedException | IOException e) {
						root.error(e.getMessage());
					}
				}
			}, "Status Metric Thread").start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void processCommand(MessageCreateEvent event) {
		//Logger logger = (Logger)LoggerFactory.getLogger("ninja.natsuko.bot.Main");
		if(event.getMessage().getUserData().bot().toOptional().orElse(false)) return;
		
		try {
			if(!event.getMember().isPresent()) return; //what
			
			Map<String,Object> opts = Main.db.getCollection(guildsString).find(Utilities.guildToFindDoc(event.getGuild().block())).first().get("options", new HashMap<>());
			messagesLastSecond.put(event.getMember().get().getId(), messagesLastSecond.getOrDefault(event.getMember().get().getId(),0)+1);
			if(messagesLastSecond.getOrDefault(event.getMember().get().getId(),0)>=(Integer)opts.getOrDefault("automod.antispam.mpslimit",3)) {
				if(opts.getOrDefault("automod.antispam","on").toString().equals("on")) {
					event.getMessage().delete().subscribe();
					exceededAntispamLimit.put(event.getMember().get().getId(),exceededAntispamLimit.getOrDefault(event.getMember().get().getId(),0)+1);
					if(exceededAntispamLimit.get(event.getMember().get().getId())>(Integer)opts.getOrDefault("automod.antispam.threshold",3)) {
						Document guildoc = Main.db.getCollection(guildsString).find(Utilities.guildToFindDoc(event.getGuild().block())).first();
						List<Document> strikes = guildoc.get(strikesString, new ArrayList<>());
						List<Document> temp = strikes.stream().filter(a->a.getLong("id") == event.getMember().get().getId().asLong()).collect(Collectors.toList());
						Document userStrikes;
						if(temp.size() < 1) {
							userStrikes = Document.parse(blockopenQuoteIdQuoteColonString+event.getMember().get().getId().asLong()+",\"strikes\":1}");
							strikes.add(userStrikes);
						} else {
							userStrikes = temp.get(0);
							int i = strikes.indexOf(userStrikes);
							userStrikes.put(strikesString, userStrikes.getInteger(strikesString)+1);
							strikes.set(i, userStrikes);
						}
						guildoc.put(strikesString, strikes);
						Main.db.getCollection(guildsString).replaceOne(Utilities.guildToFindDoc(event.getGuild().block()), guildoc);
						Utilities.processStrike(event.getMember().get(),userStrikes.getInteger(strikesString));
						ModLogger.logCase(event.getGuild().block(), ModLogger.newCase(event.getMember().get(), event.getMember().get(), "Natsuko auto-strike for antispam", null, CaseType.STRIKE, 1, event.getGuild().block()));
					}
					return;
				}
			}
			
			String msg;
			if (event.getMessage().getContent() != null) {
				msg = event.getMessage().getContent();
			} else {
				return;
			}
			
			/*if(opts.getOrDefault("automod.anticopypasta","off").toString().equals("on")) {
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
			
			if(cmd.equalsIgnoreCase("kill") && event.getMember().get().getId().asLong() == event.getClient().getSelfId().asLong()) {
				if(!msg.contains(inst)) {
					Utilities.reply(event.getMessage(), "Instance " + inst + "shutting down." );
					System.exit(0);
				}
			}
			
			if (!msg.startsWith("n;")) {
				if(!modengine.containsKey(event.getGuild().block().getId())) {
					modengine.put(event.getGuild().block().getId(),new ScriptRunner(event.getGuild().block()));
				}
				modengine.get(event.getGuild().block().getId()).run(event.getMessage());
				return;
			}
			
			String[] args = ArgumentParser.toArgs(msg).stream().skip(1).toArray(String[]::new);

			if (commands.get(cmd) != null) {
				commands.get(cmd).execute(args, event);
				return; // stop execution from continuing so that you dont have hard commands conflicting with modengine
			}
			
			//command execution takes execution precedence over modengine
			if(!modengine.containsKey(event.getGuild().block().getId())) {
				modengine.put(event.getGuild().block().getId(),new ScriptRunner(event.getGuild().block()));
			}
			modengine.get(event.getGuild().block().getId()).run(event.getMessage());
			
		} catch(Exception e) {
			
			ErrorHandler.handle(e,event);
			
		}
	}
}
