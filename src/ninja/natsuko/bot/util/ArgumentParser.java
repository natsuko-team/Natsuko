package ninja.natsuko.bot.util;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.Main;

public class ArgumentParser {

	public static User toUser(String user) {
		return Main.client.getUserById(Snowflake.of(user)).block();
	}
	public static Member toMember(String user, String guild) {
		return toGuild(guild).getMemberById(Snowflake.of(user)).block();
	}
	public static Guild toGuild(String guild) {
		return Main.client.getGuildById(Snowflake.of(guild)).block();
	}
	public static Channel toChannel(String channel) {
		return Main.client.getChannelById(Snowflake.of(channel)).block();
	}
	public static byte toByte(String bytee) {
		return Byte.parseByte(bytee);
	}
	public static short toShort(String shorte) {
		return Short.parseShort(shorte);
	}
	public static int toInt(String inte) {
		return Integer.parseInt(inte);
	}
	public static long toLong(String longe) {
		return Long.parseLong(longe);
	}
	public static float toFloat(String floate) {
		return Float.parseFloat(floate);
	}
	public static double toDouble(String doube) {
		return Double.parseDouble(doube);
	}
}
