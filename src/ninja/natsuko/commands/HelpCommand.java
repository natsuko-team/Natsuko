package ninja.natsuko.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.main.Main;
import ninja.natsuko.main.Utilities;

public class HelpCommand extends Command {

	public HelpCommand() {
		super("help", "Display command list and usage information. Usage: n;help <command>");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		if(args.length > 0) {
			Utilities.reply(e.getMessage(),Main.commands.get(args[0]).description);
			return;
		}
		StringBuilder b = new StringBuilder("Commands:\n```\n");
		for(String i : Main.commands.keySet()) {
			b.append(i+", ");
		}
		b = b.replace(b.length()-2, b.length(), "");
		b.append("```\nUsage: n;help <command>");
		String theOutput = b.toString();
		Utilities.reply(e.getMessage(), spec->{spec.setContent(theOutput);});
		return;
	}

}
