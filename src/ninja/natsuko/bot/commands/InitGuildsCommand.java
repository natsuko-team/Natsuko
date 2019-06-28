package ninja.natsuko.bot.commands;

import java.util.stream.Collectors;

import org.bson.Document;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.util.Utilities;

@Invisible
public class InitGuildsCommand extends Command {

	public InitGuildsCommand() {
		super("initguilds", "Initialize all uninitialized guilds");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		if(!Utilities.userIsStaff(e.getMember().get())) {
			Utilities.reply(e.getMessage(), "You arent staff. GTFO.");
			return;
		}
		int counter = 0;
		for(Guild i : e.getClient().getGuilds().collect(Collectors.toList()).block()) {
			Document guild = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(i)).first();
			if(guild == null) {
				Main.db.getCollection("guilds").insertOne(Utilities.initGuild(i));
				counter++;
				continue;
			}
		}
		Utilities.reply(e.getMessage(), "Initialized "+counter+" Uninitialized guilds.");
	}

}
