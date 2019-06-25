package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class KickCommand extends Command {

	public KickCommand() {
		super("test", "Testing the dynamic loader");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		e.getMessage().getChannel().block().createMessage("THE DYNAMIC LOADER WORKED HOLY SHIT").subscribe();
	}

}
