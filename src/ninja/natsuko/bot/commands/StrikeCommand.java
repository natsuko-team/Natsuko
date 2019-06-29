package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;

@Invisible
public class StrikeCommand extends Command {

	public StrikeCommand() {
		super("Strike", "Add a strike to a user");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		//TODO
	}

}
