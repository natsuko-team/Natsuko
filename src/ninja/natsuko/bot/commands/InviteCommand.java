package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class InviteCommand extends Command {

	public InviteCommand() {
		super("invite","Display the bot's invite link. Usage: n;invite");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		e.getMessage().getChannel().block().createMessage("Invite me with https://discordapp.com/oauth2/authorize?client_id="+e.getClient().getSelfId().get().asString()+"&scope=bot !").subscribe();
		return;
	}

}
