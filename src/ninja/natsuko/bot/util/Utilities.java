package ninja.natsuko.bot.util;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.MessageCreateSpec;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.moderation.ModLogger;

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
		if(userIsStaff(member)) return true;
		if(userIsAdministrator(member)) return true;
		if(member.getBasePermissions().block().contains(Permission.MANAGE_MESSAGES)) return true;
		if(member.getBasePermissions().block().contains(Permission.BAN_MEMBERS)) return true;
		if(member.getBasePermissions().block().contains(Permission.KICK_MEMBERS)) return true;
		Map<String,Object> opts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(member.getGuild().block())).first().get("options", new HashMap<>());
		Object modRole = opts.get("modrole");
		if(modRole == null) {
			return false;
		}
		if(member.getRoleIds().contains(Snowflake.of(Long.parseLong(modRole.toString())))) {
			return true;
		}
		return false;
	}
	
	public static boolean userIsAdministrator(Member member) {
		if(userIsStaff(member)) return true;
		if(member.getBasePermissions().block().contains(Permission.ADMINISTRATOR) || member.getGuild().block().getOwner().block().equals(member)) return true;
		Map<String,Object> opts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(member.getGuild().block())).first().get("options", new HashMap<>());
		Object adminRole = opts.get("adminrole");
		if(adminRole == null) {
			return false;
		}
		if(member.getRoleIds().contains(Snowflake.of(Long.parseLong(adminRole.toString())))) {
			return true;
		}
		return false;
	}
	
	public static boolean userIsStaff(User user) {
		List<Snowflake> members = Main.client.getGuildById(Snowflake.of(591720852891107328L)).block() // natsuko bot guild
				.getMembers() //get members
				.filter(m->m.getRoleIds().contains(Snowflake.of(593506464816168992L))) // only staff
				.collectList().block().stream().map(m->m.getId()).collect(Collectors.toList()); // make it a list
		
		return members.contains(user.getId());
	}

	public static Document guildToFindDoc(Guild guild) {
		return Document.parse("{\"id\":"+guild.getId().asString()+"}");
	}
	
	public static Document initGuild(Guild guild) {
		Document guildoc = guildToFindDoc(guild);
		guildoc.put("options", new HashMap<>());
		guildoc.put("modlog", new ArrayList<>());
		guildoc.put("scripts", new ArrayList<>());
		return guildoc;
	}
	
	public static boolean isNumbers(String string) {
		return string.matches("^\\d*$");
	}

	public static void processStrike(Member target, Integer strikes) {
		Map<String,Object> opts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(target.getGuild().block())).first().get("options", new HashMap<>());
		if(strikes >= (Integer)opts.getOrDefault("strikes.banThreshold", 3)){
			Matcher m = Pattern.compile("^(?:-t|--temp)=(\\d+)(m|h|d|w)$",Pattern.CASE_INSENSITIVE).matcher(opts.getOrDefault("strikes.bantime", "0m").toString());
			boolean permanent = true;
			if(!opts.getOrDefault("strikes.bantime", "0m").toString().equals("0m")) permanent = false;
			long tempTime = 0l;
			if(!m.find()) permanent = true;
			else {
				long time = Long.parseLong(m.group(1));
				String unit = m.group(2).toLowerCase();
				switch(unit) {
				case "m":
					time = time*60*1000;
					break;
				case "h":
					time = time*60*60*1000;
					break;
				case "d":
					time = time*24*60*60*1000;
					break;
				case "w":
					time = time*7*24*60*60*1000;
					break;
				default:
					break;
				}
				tempTime = Instant.now().plusMillis(time).toEpochMilli();
			}
			target.ban(b->{
				b.setReason("Natsuko auto-ban for exceeding strike threshold");
				b.setDeleteMessageDays(1);
			}).subscribe();
			if(!permanent) {
				Main.db.getCollection("timed").insertOne(Document.parse("{\"type\":\"unban\",\"guild\":"+target.getGuild().block().getId().asString()+",\"target\":\""+target.getId().asString()+"\",\"due\":"+tempTime+"}"));
			}
			ModLogger.logCase(target.getGuild().block(), ModLogger.newCase(target, Main.client.getSelf().block(), "Natsuko auto-ban for exceeding strike threshold.", permanent?null:Instant.ofEpochMilli(tempTime), CaseType.BAN, 0, target.getGuild().block()));
		}
		if(strikes >= (Integer)opts.getOrDefault("strikes.kickThreshold", 2)){
			target.kick("Natsuko auto-kick for exceeding strike threshold.").subscribe();
			ModLogger.logCase(target.getGuild().block(), ModLogger.newCase(target, Main.client.getSelf().block(), "Natsuko auto-kick for exceeding strike threshold.", null, CaseType.KICK, 0, target.getGuild().block()));
		}
	}
}
