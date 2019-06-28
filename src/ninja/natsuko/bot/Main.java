package ninja.natsuko.bot;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ini4j.Wini;
import org.reflections.Reflections;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

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
	
	static String inst = "null";
	public static void main(String[] args) {
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
			e.printStackTrace(); //TODO log properly kthhnx
		}
	}
}