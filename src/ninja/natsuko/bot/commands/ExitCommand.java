package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.presence.Presence;
import ninja.natsuko.bot.util.Utilities;

@Invisible
public class ExitCommand extends Command {

	public ExitCommand() {
		super("exit", "Kill the bot instance. Usage: n;exit");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		if(!Utilities.userIsStaff(e.getMember().get())) {
			Utilities.reply(e.getMessage(), "You arent staff. GTFO.");
			return;
		}
		e.getClient().updatePresence(Presence.invisible());
		System.exit(0);
	}

}
