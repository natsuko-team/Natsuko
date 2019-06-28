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
		if(!Utilities.userIsAdministrator(e.getMember().get())) return;
		List<String> aargs = ArgumentParser.toArgs(String.join(" ", args));
		if(Main.db.getCollection("guilds").countDocuments(Utilities.guildToFindDoc(e.getGuild().block())) == 0) {
			Main.db.getCollection("guilds").insertOne(Utilities.initGuild(e.getGuild().block()));
		}
		Map<String,Object> opts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first().get("options", new HashMap<>());
		switch(aargs.get(0)) {
		case "show":
			StringBuilder output = new StringBuilder("Config Options:```\n");
			
			for(String i : opts.keySet()) {
				output.append(i+": "+opts.get(i));
			}
			output.append("\n```");
			Utilities.reply(e.getMessage(), output.toString());
			return;
		case "set":
			Document guild = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first();
			switch(aargs.get(1)) {
			case "modrole":
				if(Utilities.isNumbers(aargs.get(2).replaceAll("[<@&>]", ""))) {
					long modRole = Long.parseLong(aargs.get(2).replaceAll("[<@&>]", ""));
					if(e.getGuild().block().getRoleById(Snowflake.of(modRole)) == null) {
						opts.put("modrole",modRole);
						guild.put("options", opts);
						Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
						Utilities.reply(e.getMessage(), "Set modrole to "+modRole);
						return;
					}
				}
				Utilities.reply(e.getMessage(), "Invalid value! Expected: number got:"+aargs.get(2));
				return;
			case "adminrole":
				if(Utilities.isNumbers(aargs.get(2).replaceAll("[<@&>]", ""))) {
					long modRole = Long.parseLong(aargs.get(2).replaceAll("[<@&>]", ""));
					if(e.getGuild().block().getRoleById(Snowflake.of(modRole)) == null) {
						opts.put("adminrole",modRole);
						guild.put("options", opts);
						Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
						Utilities.reply(e.getMessage(), "Set adminrole to "+modRole);
						return;
					}
				}
				Utilities.reply(e.getMessage(), "Invalid value! Expected: number got:"+aargs.get(2));
				return;
			case "mutedrole":
				if(Utilities.isNumbers(aargs.get(2).replaceAll("[<@&>]", ""))) {
					long modRole = Long.parseLong(aargs.get(2).replaceAll("[<@&>]", ""));
					if(e.getGuild().block().getRoleById(Snowflake.of(modRole)) == null) {
						opts.put("mutedrole",modRole);
						guild.put("options", opts);
						Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
						Utilities.reply(e.getMessage(), "Set mutedrole to "+modRole);
						return;
					}
				}
				Utilities.reply(e.getMessage(), "Invalid value! Expected: number got:"+aargs.get(2));
				return;
			default:
				Utilities.reply(e.getMessage(),"Invalid option!");
				return;
			}
		default:
			Utilities.reply(e.getMessage(),"Invalid subcommand! Expected: show, set Got: " + aargs.get(0));
			break;
		}
	}

}
