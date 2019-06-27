package ninja.natsuko.bot.scriptengine;

import discord4j.core.object.entity.TextChannel;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.util.Utilities;

public class SafeUtils {

	private static TextChannel unsafe(SafeChannel channel) {
		return (TextChannel) Main.client.getChannelById(channel.getId()).block();
	}
	
	public static SafeMessage send(SafeChannel channel, String string) {
		return new SafeMessage(Utilities.sendMessageBlocking(unsafe(channel), string));
	}
	
}
