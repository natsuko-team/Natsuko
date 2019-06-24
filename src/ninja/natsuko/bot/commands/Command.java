package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;

public abstract class Command {
	public String commandName;
	public String description;
	
	public Command(String commandName, String description) {
		this.commandName = commandName;
		this.description = description;
	}
	
	public abstract void execute(String[] args, MessageCreateEvent e);
}
