package ninja.natsuko.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class HelpCommand extends Command {

	public HelpCommand() {
		super("help", "Display command list and usage information");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		
	}

}
