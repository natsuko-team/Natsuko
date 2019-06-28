package ninja.natsuko.bot.moderation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.util.Utilities;

public class ModLogger {
	
	public static void logCase(Guild guild, Case modlogcase) {
		Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(guild)).first();
		List<Object> modlog = guildoc.get("modlog", new ArrayList<>());
		modlog.add(modlogcase.toDocument());
		guildoc.put("modlog", modlog);
	}
	
	public static Case newCase(User target, User moderator, String reason, Instant expires, CaseType type, int strikes,Guild guild) {
		Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(guild)).first();
		return new Case(target,moderator,Instant.now(),type,expires,reason,strikes,guildoc.get("modlog",new ArrayList<>()).size());
	}
}
