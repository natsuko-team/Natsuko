package ninja.natsuko.bot.util;

import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;
import ninja.natsuko.bot.Main;

public class Utilities {
	public static String longMilisToTime(long ms) {
        long time = ms / 1000;
        long seconds = time % 60;
        time /= 60;
        long minutes = time % 60;
        time /= 60;
        long hours = time % 24;
        time /= 24;
        long days = time;
        
        String strseconds = Long.toString(seconds).length() == 1 ? '0' + Long.toString(Math.round(Math.floor(seconds))): Long.toString(Math.round(Math.floor(seconds)));
        String strminutes = Long.toString(minutes).length() == 1 ? '0' + Long.toString(Math.round(Math.floor(minutes))) : Long.toString(Math.round(Math.floor(minutes)));
        String strhours = Long.toString(hours).length() == 1 ? '0' + Long.toString(Math.round(Math.floor(hours))) : Long.toString(Math.round(Math.floor(hours)));

        return days+(days == 1 ? " Day, " : " Days, ")+strhours+":"+strminutes+":"+strseconds;
    }
	
	 public static Color embedColor = new Color(252, 113, 20);
	
	
	// message helpers \\
	
	// returns message but blocks
	public static Message sendMessageBlocking(MessageChannel chan, Consumer<? super MessageCreateSpec> spec) {
		return chan.createMessage(spec).block();
	}
	
	//returns message but blocks AND string
	public static Message sendMessageBlocking(MessageChannel chan, String content) {
		return chan.createMessage(content).block();
	}
	
	// ignores message but async
	public static void sendMessage(MessageChannel chan, Consumer<? super MessageCreateSpec> spec) {
		chan.createMessage(spec).subscribe();
	}
	
	//ignores message but async AND string
	public static void sendMessage(MessageChannel chan, String content) {
		chan.createMessage(content).subscribe();
	}
	
	// reply helpers \\
	
	// returns message but blocks
	public static Message replyBlocking(Message m, Consumer<? super MessageCreateSpec> spec) {
		return m.getChannel().block().createMessage(spec).block();
	}

	// returns message but blocks and string
	public static Message replyBlocking(Message m, String content) {
		return m.getChannel().block().createMessage(content).block();
	}
	
	// ignores message but async
	public static void reply(Message m, Consumer<? super MessageCreateSpec> spec) {
		m.getChannel().block().createMessage(spec).subscribe();
	}
	
	// ignores message but async and string
	public static void reply(Message m, String content) {
		m.getChannel().block().createMessage(content).subscribe();
	}
	
	// extras \\
	
	// just shorthand
	public static MessageChannel getChannel(MessageCreateEvent e) {
		return e.getMessage().getChannel().block();
	}
	
	public static boolean userIsModerator(Member member) {
		if(member.getBasePermissions().block().contains(Permission.MANAGE_MESSAGES)) return true;
		if(member.getBasePermissions().block().contains(Permission.BAN_MEMBERS)) return true;
		if(member.getBasePermissions().block().contains(Permission.KICK_MEMBERS)) return true;
		Long modRole = Main.db.getCollection("guilds").find(org.bson.Document.parse("")).first().getLong("modRole");
		if(modRole == null) {
			return false;
		}
		if(member.getRoleIds().contains(Snowflake.of(modRole))) {
			return true;
		}
		return false;
	}
	
	public static boolean userIsAdministrator(Member member) {
		if(member.getBasePermissions().block().contains(Permission.ADMINISTRATOR) || member.getGuild().block().getOwner().block().equals(member)) return true;
		Long adminRole = Main.db.getCollection("guilds").find(org.bson.Document.parse("")).first().getLong("adminRole");
		if(adminRole == null) {
			return false;
		}
		if(member.getRoleIds().contains(Snowflake.of(adminRole))) {
			return true;
		}
		return false;
	}
	
	public static boolean userIsStaff(User user) {
		List<Member> members = Main.client.getGuildById(Snowflake.of(591720852891107328L)).block() // natsuko bot guild
				.getMembers().collectList().block(); // get members

		members.removeIf(m -> !m.getRoles().any(r -> r.getId() == Snowflake.of(593506464816168992L)).block()); // filter out people without staff role
		members.removeIf(m -> !(m.getId() == user.getId())); // filter out every other user than the user we're looking for
		
		return members.size() == 1;
	}
}
