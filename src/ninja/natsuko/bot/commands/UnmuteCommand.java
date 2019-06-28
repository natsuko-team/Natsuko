package ninja.natsuko.bot.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.moderation.ModLogger;
import ninja.natsuko.bot.util.ArgumentParser;
import ninja.natsuko.bot.util.Utilities;

public class UnmuteCommand extends Command {

	public UnmuteCommand() {
		super("unmute", "Unmute a user.");
	}

	
	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		List<String> actualArgs = ArgumentParser.toArgs(String.join(" ", args));
		Map<String,Object> opts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first().get("options", new HashMap<>());
		boolean unmuteAll = false;
		boolean silent = false;
		if(!opts.containsKey("mutedrole")) {
			Utilities.reply(e.getMessage(), "The Muted Role has not been set! Please set it with `n;config set mutedrole <role id or mention>`");
			return;
		}
		if(!e.getMember().isPresent()) return;
		if(Utilities.userIsModerator(e.getMember().get())) {
			for(String i : actualArgs) {
				if(i.matches("-s|--silent")) {
					silent = true;
					continue;
				}
				if(i.matches("^-A|--all$")){
					unmuteAll = true;
					continue;
				}
			}
			if(Utilities.isNumbers(actualArgs.get(0).replaceAll("[<@!>]", ""))) {
				Member target = ArgumentParser.toMemberByID(actualArgs.get(0).replaceAll("[<!@>]", ""), e.getGuild().block());
				if(target != null) {
					if(target.isHigher(e.getMember().get()).block()) {
						Utilities.reply(e.getMessage(), "That user is above you!");
						return;
					}
					if(target.isHigher(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block()).block()) {
						Utilities.reply(e.getMessage(), "That user is above the bot!");
						return;
					}
					if(!(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block().getBasePermissions().block().contains(Permission.BAN_MEMBERS))) {
						Utilities.reply(e.getMessage(), "I don't have permissions to manage roles!");	
						return;
					}
					target.removeRole(Snowflake.of(opts.get("mutedrole").toString()));
					ModLogger.logCase(e.getGuild().block(), ModLogger.newCase(target, e.getMember().get(), String.join(" ", args).substring(args[0].length()+1), null, CaseType.UNMUTE, 0, e.getGuild().block()));
					if(!silent) {
						Utilities.reply(e.getMessage(), e.getMember().get().getMention() + " Unmuted "+target.getUsername());
						return;
					}
					return;
				}
				
			}
			List<Member> partialresult = ArgumentParser.toMemberByPartial(actualArgs.get(0), e.getGuild().block());	
			if(partialresult.size() > 1 && !unmuteAll) {
				Utilities.reply(e.getMessage(), "There are more than one member that match that input!\n```\n"+
						String.join("\n", partialresult.stream().map(a->{return a.getUsername()+"#"+a.getDiscriminator();}).collect(Collectors.toList()))+"\n```");
				return;
			}
			if(partialresult.size() < 1) {
				Utilities.reply(e.getMessage(), "No members matched! Check your input and try again!");
			}
			StringBuilder output = new StringBuilder("");
			for(Member target : partialresult) {
				if(target.isHigher(e.getMember().get()).block()) {
					output.append("That user is above you!");
					continue;
				}
				if(target.isHigher(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block()).block()) {
					output.append("That user is above the bot!");
					continue;
				}
				if(!(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block().getBasePermissions().block().contains(Permission.MANAGE_ROLES))) {
					output.append("I don't have permissions to manage roles!");	
					return;
				}
				target.removeRole(Snowflake.of(opts.get("mutedrole").toString()));
				ModLogger.logCase(e.getGuild().block(), ModLogger.newCase(target, e.getMember().get(), String.join(" ", args).substring(args[0].length()+1), null, CaseType.UNMUTE, 0, e.getGuild().block()));
				if(!silent) {
					output.append(e.getMember().get().getMention() + " Unmuted "+target.getUsername());
					continue;
				}
			}
			Utilities.reply(e.getMessage(), output.toString());
			return;
		}
		Utilities.reply(e.getMessage(), "You dont have permissions to ban!");
		return;
	}
	
}
