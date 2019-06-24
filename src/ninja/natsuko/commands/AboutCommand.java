package ninja.natsuko.commands;

import java.lang.management.ManagementFactory;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.main.Utilities;

public class AboutCommand extends Command {

	public AboutCommand() {
		super("about", "Displays information about the bot.");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		e.getMessage().getChannel().block().createMessage(spec->{
			spec.setEmbed(espec->{
				espec.setAuthor("Natsuko", "https://natsuko.ninja", "https://natsuko.ninja");
				espec.addField("Servers", e.getClient().getGuilds().count().block().toString(), true);
				espec.addField("Uptime", Utilities.longMilisToTime(ManagementFactory.getRuntimeMXBean().getUptime()), true);
				espec.addField("Memory", Long.toString((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000/1000)+"MB", true);
			});
		}).subscribe();
		return;
	}
	
}
