package ninja.natsuko.bot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.Main;

public class ArgumentParser {

	public static List<String> toArgs(String string){
		List<String> temp = new ArrayList<>();
		Matcher baseMatcher = Pattern.compile("(?<=^|(?<=^|[^\\\\])(?:\\\\\\\\)*)([\"']|`(?:``))(?:|(.*?)(?<=[^\\\\])(?:\\\\\\\\)*)\\1|(\\S+)",Pattern.DOTALL | Pattern.MULTILINE).matcher(string);
		while (baseMatcher.find()) {
			String quoted = baseMatcher.group(3);
			String noquot = baseMatcher.group(4);
			String match = quoted == null ? noquot : quoted;
			//if(match.matches("^([\"']|`(?:``)).+\"$")) match = match.substring(1, match.length()-1);
			if(match.length() == 0) continue;
		    temp.add(match);
		}
		return temp;
	}
	
	public static User toUserByID(String user) {
		return Main.client.getUserById(Snowflake.of(user)).block();
	}
	public static List<User> toUserByDTag(String dtag) {
		return Main.client.getUsers().filter(m->{return (m.getUsername()+"#"+m.getDiscriminator() == dtag);}).collect(Collectors.toList()).block();
	}
	public static List<User> toUserByPartial(String partial) {
		return Main.client.getUsers().filter(m->{return ((m.getUsername().toLowerCase()+"#"+m.getDiscriminator()).contains(partial.toLowerCase()));}).collect(Collectors.toList()).block();
	}
	public static List<Member> toMemberByDTag(String dtag,Guild guild) {
		return guild.getMembers().collectList().block().stream().filter(m->{return (m.getUsername()+"#"+m.getDiscriminator()).equals(dtag);}).collect(Collectors.toList());
	}
	public static List<Member> toMemberByPartial(String partial,Guild guild) {
		return guild.getMembers().filter(m->{return ((m.getUsername().toLowerCase()+"#"+m.getDiscriminator()).contains(partial.toLowerCase()));}).collect(Collectors.toList()).block();
	}
	public static Member toMemberByID(String user, Guild guild) {
		return guild.getMemberById(Snowflake.of(user)).block();
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
