package ninja.natsuko.bot.commands;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.util.ArgumentParser;
import ninja.natsuko.bot.util.Utilities;

public class ScriptCommand extends Command {

	public ScriptCommand() {
		super("script", "Show, Add or Edit scripts on the moderation engine.");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		if(!Utilities.userIsAdministrator(e.getMember().get())) return;
		List<String> aargs = ArgumentParser.toArgs(String.join(" ", args));
		Document guild = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first();
		List<String> scripts = guild.get("scripts",new ArrayList<>());
		switch(aargs.get(0)) {
		case "show":
			if(Utilities.isNumbers(aargs.get(1))) {
				Utilities.reply(e.getMessage(), "```js\n"+scripts.get(Integer.parseInt(aargs.get(1))-1)+"```");
				return;
			}
			StringBuilder output = new StringBuilder("Script Slots:```");
			for(int i = 0; i < scripts.size();i++) {
				output.append((i+1)+": Used");
			}
			for(int i = scripts.size(); i < 6;i++) {
				output.append(i>3?(i+1)+": *RESERVED*":(i+1)+": Free");
			}
			break;
		case "add":
			if(scripts.size() < 3) {
				scripts.add(aargs.get(1));
				guild.put("scripts", scripts);
				Main.db.getCollection("guilds").updateOne(Utilities.guildToFindDoc(e.getGuild().block()), guild);
				Utilities.reply(e.getMessage(), "Added script "+scripts.size());
				return;
			}
			break;
		case "edit":
			if(Utilities.isNumbers(aargs.get(1))) {
				int scriptToEdit = Integer.parseInt(aargs.get(1))-1;
				if(scripts.get(scriptToEdit) != null) {
					scripts.set(scriptToEdit, aargs.get(2));
					guild.put("scripts", scripts);
					Main.db.getCollection("guilds").updateOne(Utilities.guildToFindDoc(e.getGuild().block()), guild);
					Utilities.reply(e.getMessage(), "Edited script "+scriptToEdit+1);
					return;
				}
			}
			break;
		case "delete":
			if(Utilities.isNumbers(aargs.get(1))) {
				int scriptToEdit = Integer.parseInt(aargs.get(1))-1;
				if(scripts.get(scriptToEdit) != null) {
					scripts.remove(scriptToEdit);
					guild.put("scripts", scripts);
					Main.db.getCollection("guilds").updateOne(Utilities.guildToFindDoc(e.getGuild().block()), guild);
					Utilities.reply(e.getMessage(), "Deleted script "+scriptToEdit+1);
				}
			}
			break;
		default:
			Utilities.reply(e.getMessage(), "Invalid subcommand! Expected: show, add, edit Got:" + aargs.get(0));
			break;
		
		}
	}

}
