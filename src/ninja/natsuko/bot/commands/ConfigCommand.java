package ninja.natsuko.bot.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.util.ArgumentParser;
import ninja.natsuko.bot.util.Utilities;

public class ConfigCommand extends Command {

	public ConfigCommand() {
		super("config", "Configure Natsuko settings for your server. Usage: n;config <show|set> [if:set/<option> <value>]\n"
				+ "See https://docs.natsuko.ninja/guides/getting-started#configuration");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		if(!Utilities.userIsAdministrator(e.getMember().get())) {
			Utilities.reply(e.getMessage(), "You dont have permission to do this!");
			return;
		}
		List<String> aargs = ArgumentParser.toArgs(String.join(" ", args));
		if(Main.db.getCollection("guilds").countDocuments(Utilities.guildToFindDoc(e.getGuild().block())) == 0) {
			Main.db.getCollection("guilds").insertOne(Utilities.initGuild(e.getGuild().block()));
		}
		if(aargs.size() == 0) {
			Utilities.reply(e.getMessage(), this.description);
			return;
		}
		Map<String,Object> opts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first().get("options", new HashMap<>());
		switch(aargs.get(0)) {
		case "show":
			StringBuilder output = new StringBuilder("Config Options:```\n");
			
			for(String i : opts.keySet()) {
				output.append(i+": "+opts.get(i)+"\n");
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
					if(e.getGuild().block().getRoleById(Snowflake.of(modRole)) != null) {
						opts.put("modrole",modRole);
						guild.put("options", opts);
						Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
						Utilities.reply(e.getMessage(), "Set Mod-Role to <@&"+modRole+">");
						return;
					}
				}
				Utilities.reply(e.getMessage(), "Invalid value! Expected: Role got:"+aargs.get(2).replaceAll("[<@&>]", ""));
				return;
			case "adminrole":
				if(Utilities.isNumbers(aargs.get(2).replaceAll("[<@&>]", ""))) {
					long modRole = Long.parseLong(aargs.get(2).replaceAll("[<@&>]", ""));
					if(e.getGuild().block().getRoleById(Snowflake.of(modRole)) != null) {
						opts.put("adminrole",modRole);
						guild.put("options", opts);
						Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
						Utilities.reply(e.getMessage(), "Set Admin-Role to <@&"+modRole+">");
						return;
					}
				}
				Utilities.reply(e.getMessage(), "Invalid value! Expected: Role got:"+aargs.get(2).replaceAll("[<@&>]", ""));
				return;
			case "mutedrole":
				if(Utilities.isNumbers(aargs.get(2).replaceAll("[<@&>]", ""))) {
					long modRole = Long.parseLong(aargs.get(2).replaceAll("[<@&>]", ""));
					if(e.getGuild().block().getRoleById(Snowflake.of(modRole)) != null) {
						opts.put("mutedrole",modRole);
						guild.put("options", opts);
						Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
						Utilities.reply(e.getMessage(), "Set Muted-Role to <@&"+modRole+">");
						return;
					}
				}
				Utilities.reply(e.getMessage(), "Invalid value! Expected: Role got:"+aargs.get(2).replaceAll("[<@&>]", ""));
				return;
			case "modlog":
				if(Utilities.isNumbers(aargs.get(2).replaceAll("[<@#>]", ""))) {
					long modRole = Long.parseLong(aargs.get(2).replaceAll("[<@#>]", ""));
					if(e.getGuild().block().getChannelById(Snowflake.of(modRole)) != null) {
						opts.put("modlog",modRole);
						guild.put("options", opts);
						Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
						Utilities.reply(e.getMessage(), "Set Modlog Channel to <#"+modRole+">");
						return;
					}
				}
				Utilities.reply(e.getMessage(), "Invalid value! Expected: Channel got:"+aargs.get(2).replaceAll("[<@&>]", ""));
				return;
			case "strikes.kickthreshold":
				if(Utilities.isNumbers(aargs.get(2))) {
					Integer thresh = ArgumentParser.toInt(aargs.get(2));
					opts.put("strikes.kickthreshold",thresh);
					guild.put("options", opts);
					Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
					Utilities.reply(e.getMessage(), "Set Kick Threshold to "+thresh+"");
					return;
				}
				Utilities.reply(e.getMessage(), "Invalid value! Expected: Integer got:"+aargs.get(2));
				return;
			case "strikes.banthreshold":
				if(Utilities.isNumbers(aargs.get(2))) {
					Integer thresh = ArgumentParser.toInt(aargs.get(2));
					opts.put("strikes.banthreshold",thresh);
					guild.put("options", opts);
					Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
					Utilities.reply(e.getMessage(), "Set Ban Threshold to "+thresh+"");
					return;
				}
				Utilities.reply(e.getMessage(), "Invalid value! Expected: Integer got:"+aargs.get(2));
				return;
			case "strikes.bantime":
				if(Utilities.isNumbers(aargs.get(2))) {
					String time = aargs.get(2);
					if(!time.matches("^(?:-t|--temp)=-?(\\d+)(m|h|d|w)$")) {
						Utilities.reply(e.getMessage(), "Invalid value! Expected: Time got:"+aargs.get(2));
						return;
					}
					opts.put("strikes.bantime",time);
					guild.put("options", opts);
					Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
					Utilities.reply(e.getMessage(), "Set Ban Time to "+time);
					return;
				}
				Utilities.reply(e.getMessage(), "Invalid value! Expected: Time got:"+aargs.get(2));
				return;
			case "automod.antispam":
				if(!aargs.get(2).matches("on|off")) {
					Utilities.reply(e.getMessage(), "Invalid value! Expected: number got:"+aargs.get(2));
					return;
				}
				opts.put("automod.antispam",aargs.get(2));
				guild.put("options", opts);
				Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
				Utilities.reply(e.getMessage(), "Set antispam to "+aargs.get(2));
				return;
			case "automod.antispam.mpslimit":
				if(!Utilities.isNumbers(aargs.get(2))) {
					Utilities.reply(e.getMessage(), "Invalid value! Expected: number got:"+aargs.get(2));
					return;
				}
				opts.put("automod.antispam.mpslimit",aargs.get(2));
				guild.put("options", opts);
				Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
				Utilities.reply(e.getMessage(), "Set antispam messages per second limit to "+aargs.get(2));
				return;
			case "automod.antispam.threshold":
				if(!Utilities.isNumbers(aargs.get(2))) {
					Utilities.reply(e.getMessage(), "Invalid value! Expected: number got:"+aargs.get(2));
					return;
				}
				opts.put("automod.antispam.exceededLimit",aargs.get(2));
				guild.put("options", opts);
				Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()),guild);
				Utilities.reply(e.getMessage(), "Set antispam threshold to "+aargs.get(2));
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
