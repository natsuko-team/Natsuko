package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.bot.util.Utilities;

@Invisible
public class ExitCommand extends Command {

	public ExitCommand() {
		super("exit", "Kill the bot instance.");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		if(!Utilities.userIsStaff(e.getMember().get()))
		System.exit(0);
	}

}
