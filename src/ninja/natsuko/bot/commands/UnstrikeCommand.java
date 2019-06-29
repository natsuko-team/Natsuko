package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;

@Invisible
public class UnstrikeCommand extends Command {

	public UnstrikeCommand() {
		super("Unstrike", "Remove a strike from the user.");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		//TODO
	}

}
