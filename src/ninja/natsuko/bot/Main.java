package ninja.natsuko.bot;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.commands.Command;
import ninja.natsuko.bot.scriptengine.ScriptRunner;
import ninja.natsuko.bot.util.Utilities;

public class Main {
	public static Map<String, Command> commands = new HashMap<>();
	public static MongoDatabase db;
	public static DiscordClient client;
	public static Map<Snowflake,ScriptRunner> modengine = new HashMap<>();
	public static Thread timedEventThread;
	
	static String inst = "null";
	public static void main(String[] args) {
		Logger root = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
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
								client.getGuildById(Snowflake.of(i.getLong("guild"))).block().unban(Snowflake.of(i.getString("target")), "Natsuko auto-unban after "+i.getLong("due")+"ms");
								db.getCollection("timed").deleteOne(i);
								break;
							case "unmute":
								long roleId = 0l;
								if(!opts.containsKey("mutedrole")) continue; //wtf?
								roleId = Long.parseLong(opts.get("mutedrole").toString());
								client.getGuildById(Snowflake.of(i.getLong("guild"))).block().getMemberById(Snowflake.of(i.getString("target"))).block().removeRole(
										Snowflake.of(roleId), "Natsuko auto-unmute after "+i.getLong("due")+"ms");
								db.getCollection("timed").deleteOne(i);
								break;
							case "unstrike":
								break;
							default:
								break;
							}
						} catch(Exception e) {
							db.getCollection("timed").deleteOne(i); //it failed to remove it so it doesnt fuck up more things
							e.printStackTrace(); //TODO properly log exception in timed thread
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
				return;
			});	
			client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(event->{
				if(event.getGuild().getJoinTime().get().isAfter(Instant.now().minusMillis(1000l))) {
					Main.db.getCollection("guilds").insertOne(Utilities.initGuild(event.getGuild()));
					//TODO fuck botfarms
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
			String msg;
			if (event.getMessage().getContent().isPresent()) {
				msg = event.getMessage().getContent().get();
			} else {
				return;
			}
			
			
			// commands area
			
			String[] vomit;

			try {
				vomit = msg.substring(2).split(" ");
			} catch (ArrayIndexOutOfBoundsException e) {
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
			event.getMessage().getChannel().block().createMessage(":warning: An error has occurred!\n```"+trace+"```").subscribe();
			e.printStackTrace();
		}
	}
}