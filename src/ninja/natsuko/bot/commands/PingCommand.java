package ninja.natsuko.bot.commands;

import java.time.Instant;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

public class PingCommand extends Command {

	public PingCommand() {
		super("ping", "Test the bot's aliveness. Usage: n;ping");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		long heartbeat = e.getClient().getResponseTime();
		Instant before = Instant.now();
		Message msg = e.getMessage().getChannel().block().createMessage("Heartbeat: "+heartbeat+"ms").block();
		long roundtrip = Instant.now().toEpochMilli()-before.toEpochMilli();
		msg.edit(spec->{spec.setContent("Heartbeat: "+heartbeat+"ms\nRound-Trip: "+roundtrip+"ms");}).subscribe();
		return;
	}

}
