package ninja.natsuko.bot.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.util.ArgumentParser;
import ninja.natsuko.bot.util.Utilities;

public class ConfigCommand extends Command {

	public ConfigCommand() {
		super("config", "Configure Natsuko settings for your server.");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		List<String> aargs = ArgumentParser.toArgs(String.join(" ", args));
		Map<String,Object> opts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first().get("options", new HashMap<>());
		switch(aargs.get(0)) {
		case "show":
			StringBuilder output = new StringBuilder("Config Options:```\n");
			
			for(String i : opts.keySet()) {
				output.append(i+": "+opts.get(i));
			}
			output.append("\n```");
			Utilities.reply(e.getMessage(), output.toString());
			break;
		case "set":
			switch(aargs.get(1)) {
			case "modrole":
				if(Utilities.isNumbers(aargs.get(2))) {
					long modRole = Long.parseLong(aargs.get(2));
					if(e.getGuild().block().getRoleById(Snowflake.of(modRole)) == null) {
						opts.put("modrole",modRole);
					}
				}
				Document guild = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first();
				guild.put("options", opts);
				Main.db.getCollection("guilds").updateOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
				break;
			default:
				Utilities.reply(e.getMessage(),"Invalid option!");
				break;
			}
			break;
		default:
			Utilities.reply(e.getMessage(),"Invalid subcommand! Expected: show, set Got: " + aargs.get(0));
			break;
		}
	}

}
