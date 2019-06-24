package ninja.natsuko.bot.commands;

//import java.lang.management.ManagementFactory;

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
				//embed.addField("Owner", e.getGuild().block().getMemberById(Snowflake.of(e.getClient().getSelfId().get().asLong())).block().getJoinTime());
				embed.addField("Members", e.getGuild().block().getMembers().count().block().toString(), true);
			});
		});
		return;
	}
	
}
