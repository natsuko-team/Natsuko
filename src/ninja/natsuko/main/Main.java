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
			client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
				//TODO your block lewis
			});	
			client.login().block();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}