package ninja.natsuko.main;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ini4j.Wini;
import org.reflections.Reflections;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.main.command.Command;

public class Main {
	private static Map<String, Command> commands = new HashMap<>();
	
	public static void main(String[] args) {
		Reflections reflections = new Reflections("ninja.natsuko.main.command"); // restrict to command package to prevent unnecessary searching
		Set<Class<? extends Command>> commandClasses = reflections.getSubTypesOf(Command.class);
		commandClasses.forEach((cmd) -> {
			try {
				Command cmdClass = cmd.newInstance();
				commands.put(cmdClass.commandName, cmdClass);
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		try {
			Wini config = new Wini(new File("./config.ini"));
			DiscordClientBuilder builder = new DiscordClientBuilder(config.get("Config", "token"));
			DiscordClient client = builder.build();	
			client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {
				//readyevent do things later
			});	
			client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {processCommand(event);});	
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
		String[] args = Arrays.stream(vomit).skip(1).toArray(String[]::new);

		if (commands.get(cmd) != null) {
			commands.get(cmd).execute(args, event);
		}
	}
}