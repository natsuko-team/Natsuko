package ninja.natsuko.main;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ini4j.Wini;
import org.reflections.Reflections;

import com.mongodb.MongoClientURI;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.commands.Command;

public class Main {
	private static Map<String, Command> commands = new HashMap<>();
	public static Database DATABASE;
	
	static String inst = "null";
	public static void main(String[] args) {
		Reflections reflections = new Reflections("ninja.natsuko.commands"); // restrict to command package to prevent unnecessary searching
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
			
			DATABASE = new Database(new MongoClientURI(config.get("Mongo", "uri")), config.get("Mongo", "database"));
			
			DiscordClientBuilder builder = new DiscordClientBuilder(config.get("Config", "token"));
			DiscordClient client = builder.build();	
			client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {
				((MessageChannel)event.getClient().getChannelById(Snowflake.of(592781286297305091l)).block()).createMessage("n;kill "+inst).subscribe();
				return;
			});	
			client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(Main::processCommand);
			client.login().block();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void processCommand(MessageCreateEvent event) {
		String msg;
		if (event.getMessage().getContent().isPresent()) {
			msg = event.getMessage().getContent().get();
		} else {
			return;
		}
		
		String[] vomit;

		try {
			vomit = msg.split("n;")[1].split(" ");
		} catch (ArrayIndexOutOfBoundsException e) {
			// ignore and skip
			return;
		}
		
		String cmd = vomit[0];
		
		if(cmd.equalsIgnoreCase("kill") && event.getMember().get().getId().asLong() == event.getClient().getSelfId().get().asLong()) {
			if(!msg.contains(inst))System.exit(0);
		}
		
		String[] args = Arrays.stream(vomit).skip(1).toArray(String[]::new);

		if (commands.get(cmd) != null) 
			commands.get(cmd).execute(args, event);
	}
}