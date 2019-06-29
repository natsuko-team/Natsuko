package ninja.natsuko.bot.commands;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.moderation.Case;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.util.ArgumentParser;
import ninja.natsuko.bot.util.Utilities;

public class ReasonCommand extends Command {

	public ReasonCommand() {
		super("reason","Update the reason of a modlog entry. Usage: n;reason <id> <reason>");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		if(!Utilities.userIsModerator(e.getMember().get())) {
			Utilities.reply(e.getMessage(), "You arent a moderator!");
			return;
		}
		if(args.length < 2) {
			Utilities.reply(e.getMessage(), this.description);
			return;
		}
		Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first();
		List<Document> modlog = guildoc.get("modlog", new ArrayList<>());
		Document theCase = modlog.stream().filter(d->d.getInteger("id").equals(ArgumentParser.toInt(args[0])-1)).collect(Collectors.toList()).get(0);
		Map<String,Object> opts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first().get("options", new HashMap<>());
		TextChannel chan = (TextChannel) e.getClient().getChannelById(Snowflake.of((long) opts.get("modlog"))).block();
		Message mesg = chan.getMessageById(Snowflake.of(theCase.getLong("msgid"))).block();
		int i = modlog.indexOf(theCase);
		theCase.put("reason",String.join(" ", args).substring(args[0].length()+1));
		modlog.set(i, theCase);
		guildoc.put("modlog", modlog);
		mesg.edit(m->{
			m.setContent(new Case(Main.client.getUserById(Snowflake.of(theCase.get("targetUser").toString())).block(),Main.client.getUserById(Snowflake.of(theCase.get("moderatorUser").toString())).block(),Instant.ofEpochMilli(theCase.getLong("date")),CaseType.valueOf(theCase.get("type").toString().toUpperCase()),theCase.get("expiryDate")==null?null:Instant.ofEpochMilli(theCase.getLong("expiryDate")),theCase.get("reason").toString(),theCase.getInteger("id")).toModLog());
		}).subscribe();
		Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()), guildoc);
		Utilities.reply(e.getMessage(), "Updated case "+(i+1));
	}

}
