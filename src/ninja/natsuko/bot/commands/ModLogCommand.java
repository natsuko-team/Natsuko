package ninja.natsuko.bot.commands;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.moderation.Case;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.util.ArgumentParser;
import ninja.natsuko.bot.util.Utilities;

public class ModLogCommand extends Command {

	public ModLogCommand() {
		super("modlog","Administrate the modlog. Usage: n;modlog <view|count|wipe> [if:view/id]");
	}
	
	private Map<Snowflake,Double> wipeKeys = new HashMap<>();

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		if(args.length < 1) {
			Utilities.reply(e.getMessage(), this.description);
			return;
		}
		Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first();
		List<Document> modlog = guildoc.get("modlog", new ArrayList<>());
		switch(args[0]) {
		case "view":
			if(args.length < 2 || modlog.size() < ArgumentParser.toInt(args[1])) {
				Utilities.reply(e.getMessage(), "Administrate the modlog. Usage: n;modlog view <id>");
				return;
			}
			Document entry = modlog.get(ArgumentParser.toInt(args[1]));
			Utilities.reply(e.getMessage(), new Case(Main.client.getUserById(Snowflake.of(entry.get("targetUser").toString())).block(),Main.client.getUserById(Snowflake.of(entry.get("moderatorUser").toString())).block(),Instant.ofEpochMilli(entry.getLong("date")),CaseType.valueOf(entry.get("type").toString()),entry.get("expiryDate")==null?null:Instant.ofEpochMilli(entry.getLong("expiryDate")),entry.get("reason").toString(),entry.getInteger("id")).toModLog());
			break;
		case "count":
			Utilities.reply(e.getMessage(), "Modlog entries stored: "+modlog.size());
			return;
		case "wipe":
			Double wipeKey = Math.random();
			wipeKeys.put(e.getGuild().block().getId(),wipeKey);
			Utilities.reply(e.getMessage(), ":warning: This operation is IRREVERSIBLE. Please run n;modlog confirmwipe "+wipeKey+" to continue. :warning: ");
		default:
			Utilities.reply(e.getMessage(), this.description);
			return;
		}
	}

}
