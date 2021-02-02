package ninja.natsuko.bot.moderation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.util.Utilities;

public class ModLogger {
	
	public static void logCase(Guild guild, Case modlogcase) {
		Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(guild)).first();
		List<Object> modlog = guildoc.get("modlog", new ArrayList<>());
		Map<String,Object> opts = guildoc.get("options",new HashMap<>());
		Message modlogged = null;
		if(opts.getOrDefault("modlog","").toString().length()>16) {
			Channel channel = Main.client.getChannelById(Snowflake.of(opts.get("modlog").toString())).block();
			if(channel instanceof TextChannel) {
				modlogged = Utilities.sendMessageBlocking((TextChannel)channel, modlogcase.toModLog());
			}
		}
		
		Document modlogdoc = modlogcase.toDocument();
		if(modlogged != null) {
			modlogdoc.put("msgid",modlogged.getId().asLong());
		}
		modlog.add(modlogdoc);
		guildoc.put("modlog", modlog);
		Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(guild),guildoc);
	}
	
	public static Case newCase(User target, User moderator, String reason, Instant expires, CaseType type, Guild guild) {
		Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(guild)).first();
		return new Case(target,moderator,Instant.now(),type,expires,reason,0,guildoc.get("modlog",new ArrayList<>()).size());
	}
	public static Case newCase(User target, User moderator, String reason, Instant expires, CaseType type, int amount,Guild guild) {
		Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(guild)).first();
		return new Case(target,moderator,Instant.now(),type,expires,reason,amount,guildoc.get("modlog",new ArrayList<>()).size());
	}
}
