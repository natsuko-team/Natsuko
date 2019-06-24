package ninja.natsuko.main;
import java.io.File;
import java.io.IOException;

import org.ini4j.Wini;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class Main {
	public static void main(String[] args) {
		try {
			Wini config = new Wini(new File("./config.ini"));
			DiscordClientBuilder builder = new DiscordClientBuilder(config.get("Config", "token"));
			DiscordClient client = builder.build();	
			client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {
				//readyevent do things later
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
		String cmd = msg.split("n;")[1].split(" ")[0];
		if(cmd.equalsIgnoreCase("invite")) {
			event.getMessage().getChannel().block().createMessage("Invite me with https://discordapp.com/oauth2/authorize?client_id="+event.getClient().getSelfId().get().asString()+"&scope=bot !").subscribe();
			return;
		}
		if(cmd.equalsIgnoreCase("about")) {
			//about stuff
		}
	}
}