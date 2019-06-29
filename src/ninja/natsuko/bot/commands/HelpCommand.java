package ninja.natsuko.bot.commands;

import java.util.Set;

import org.reflections.Reflections;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.util.Utilities;

public class HelpCommand extends Command {

	private Reflections reflect = new Reflections("ninja.natsuko.bot.commands");
	
	public HelpCommand() {
		super("help", "Display command list and usage information. Usage: n;help [command]");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		Set<Class<?>> invisibleCommands = this.reflect.getTypesAnnotatedWith(Invisible.class);
		
		if(args.length > 0) {
			Utilities.reply(e.getMessage(),Main.commands.get(args[0]).description);
			return;
		}
		StringBuilder b = new StringBuilder("Commands:\n```\n");
		for(String i : Main.commands.keySet()) {
			if (invisibleCommands.contains(Main.commands.get(i).getClass())) continue;
			b.append(i+", ");
		}
		b = b.replace(b.length()-2, b.length(), "");
		b.append("```\nUsage: n;help <command>");
		String theOutput = b.toString();
		Utilities.reply(e.getMessage(), spec->{spec.setContent(theOutput);});
		return;
	}

}
