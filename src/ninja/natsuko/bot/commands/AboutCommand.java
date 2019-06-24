package ninja.natsuko.bot.commands;

import java.lang.management.ManagementFactory;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.bot.util.Utilities;

public class AboutCommand extends Command {

	public AboutCommand() {
		super("about", "Displays information about the bot. Usage: n;about");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		Utilities.reply(e.getMessage(), spec -> {
			spec.setEmbed(embed -> {
				embed.setAuthor("Natsuko", "https://natsuko.ninja", "https://natsuko.ninja");
				embed.addField("Servers", e.getClient().getGuilds().count().block().toString(), true);
				embed.addField("Uptime", Utilities.longMilisToTime(ManagementFactory.getRuntimeMXBean().getUptime()), true);
				embed.addField("Memory", Long.toString((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000/1000)+"MB", true);
			});
		});
		return;
	}
	
}
