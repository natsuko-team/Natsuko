package ninja.natsuko.commands;

//import java.lang.management.ManagementFactory;

import discord4j.core.event.domain.message.MessageCreateEvent;
//import discord4j.core.object.entity.Guild;
//import discord4j.core.object.util.Snowflake;
import ninja.natsuko.main.Utilities;

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
