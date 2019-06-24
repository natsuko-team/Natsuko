package ninja.natsuko.main;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.ini4j.Wini;

import ninja.natsuko.main.Utilities;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;

public class Main {
	
	static String inst = "null";
	public static void main(String[] args) {
		inst = Double.toString(Math.random());
		try {
			Wini config = new Wini(new File("./config.ini"));
			DiscordClientBuilder builder = new DiscordClientBuilder(config.get("Config", "token"));
			DiscordClient client = builder.build();	
			client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {
				((MessageChannel)event.getClient().getChannelById(Snowflake.of(592781286297305091l)).block()).createMessage("n;kill "+inst).subscribe();
				return;
			});	
			client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {processCommand(event);});	
			client.login().block();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void processCommand(MessageCreateEvent event) {
		String msg;
		if(event.getMessage().getContent().isPresent()) {
			msg = event.getMessage().getContent().get();
		} else {
			return;
		}
		if(!msg.startsWith("n;")) return;
		String cmd = msg.split("n;")[1].split(" ")[0];
		if(cmd.equalsIgnoreCase("kill") && event.getMember().get().getId().asLong() == event.getClient().getSelfId().get().asLong()) {
			if(!msg.contains(inst))System.exit(0);
		}
		if(cmd.equalsIgnoreCase("invite")) {
			event.getMessage().getChannel().block().createMessage("Invite me with https://discordapp.com/oauth2/authorize?client_id="+event.getClient().getSelfId().get().asString()+"&scope=bot !").subscribe();
			return;
		}
		if(cmd.equalsIgnoreCase("about")) {
			event.getMessage().getChannel().block().createMessage(spec->{
				spec.setEmbed(espec->{
					espec.setAuthor("Natsuko", "https://natsuko.ninja", "https://natsuko.ninja");
					espec.addField("Servers", event.getClient().getGuilds().count().block().toString(), true);
					espec.addField("Uptime", Utilities.longMilisToTime(ManagementFactory.getRuntimeMXBean().getUptime()), true);
					espec.addField("Memory", Long.toString((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000/1000)+"MB", true);
				});
			}).subscribe();
		}
	}
}