package ninja.natsuko.bot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.bot.Main;
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
		Document theCase = modlog.stream().filter(d->d.getInteger("id").equals(ArgumentParser.toInt(args[0]))).collect(Collectors.toList()).get(0);
		int i = modlog.indexOf(theCase);
		theCase.put("reason",String.join(" ", args).substring(args[0].length()+1));
		modlog.set(i, theCase);
		guildoc.put("modlog", modlog);
		Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()), guildoc);
		
	}

}
