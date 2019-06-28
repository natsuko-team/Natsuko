package ninja.natsuko.bot.scriptengine;

import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.util.Utilities;

public class SafeUtils {

	private static TextChannel unsafeChan(SafeChannel channel) {
		return (TextChannel) Main.client.getChannelById(channel.getId()).block();
	}
	
	public static SafeMessage send(SafeChannel channel, String string) {
		return new SafeMessage(Utilities.sendMessageBlocking(unsafeChan(channel), string));
	}
	
	public static Snowflake id(String id) {
		return Snowflake.of(id);
	}
	
}
