package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import ninja.natsuko.bot.util.Utilities;

public class ServerCommand extends Command {

	public ServerCommand() {
		super("server", "Displays information about the current server. Usage: n;server");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		Utilities.reply(e.getMessage(), spec -> {
			spec.setEmbed(embed -> {
				User owner = e.getGuild().block().getOwner().block();
				embed.setAuthor("Natsuko", "https://natsuko.ninja", "https://natsuko.ninja");
				embed.addField("Owner", owner.getUsername()+"#"+owner.getDiscriminator(), true);
				embed.addField("Name", e.getGuild().block().getName().toString(), true);
				embed.addField("Members", e.getGuild().block().getMembers().count().block().toString(), true);
				embed.addField("Date Added", Long.toString( e.getGuild().block().getJoinTime().get().toEpochMilli()), true);
				embed.setColor(Utilities.embedColor);
			});
		});
		return;
	}
	
}
