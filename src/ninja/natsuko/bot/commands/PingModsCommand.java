package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.bot.Main;

public class PingModsCommand extends Command {

	public PingModsCommand() {
		super("pingmods","Ping an active moderator. Usage: n;pingmods [reason]");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		Long modRole = Main.db.getCollection("guilds").find(org.bson.Document.parse("")).first().getLong("modRole");
		if(modRole == null) {
			
		}
	}

}
