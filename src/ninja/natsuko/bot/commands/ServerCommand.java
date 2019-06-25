package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.bot.util.Utilities;

public class ServerCommand extends Command {

	public ServerCommand() {
		super("server", "Displays information about the current server.");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		Utilities.reply(e.getMessage(), spec -> {
			spec.setEmbed(embed -> {
				embed.setAuthor("Natsuko", "https://natsuko.ninja", "https://natsuko.ninja");
				embed.addField("Owner", e.getGuild().block().getOwner().toString(), true);
				embed.addField("Name", e.getGuild().block().getName().toString(), true);
				embed.addField("Members", e.getGuild().block().getMembers().count().block().toString(), true);
				embed.addField("Date Added", e.getGuild().block().getMembers().count().block().toString(), true);
				embed.setColor(Utilities.embedColor);
			});
		});
		return;
	}
	
}
