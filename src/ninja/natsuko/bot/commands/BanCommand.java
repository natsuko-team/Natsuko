package ninja.natsuko.bot.commands;

import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.moderation.ModLogger;
import ninja.natsuko.bot.util.ArgumentParser;
import ninja.natsuko.bot.util.Utilities;

public class BanCommand extends Command {

	public BanCommand() {
		super("ban", "Ban a user from the server. Usage: n;ban <Mention, ID or query> [-A/--allResults|-s/--silent|-t/-temp=\\d[m|h|d|w]|-d/--deleteDays] [reason]");
	}

	
	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		List<String> actualArgs = ArgumentParser.toArgs(String.join(" ", args));
		if(actualArgs.size() == 0) {
			Utilities.reply(e.getMessage(), this.description);
			return;
		}
		int bandays = 1;
		long tempTime = -1l;
		boolean banAll = false;
		boolean silent = false;
		if(!e.getMember().isPresent()) return;
		if(Utilities.userIsModerator(e.getMember().get())) {
			for(String i : actualArgs) {
				if(i.matches("^(?:-d|--deleteDays)=\\d$")){
					try {
						bandays = Integer.parseInt(i.split("-d=")[1]);
					} catch(NumberFormatException ex) {
						Utilities.reply(e.getMessage(), "Invalid amount of days to delete messages! Must be between 0 and 7");
						return;
					}
				}
				if(i.matches("-s|--silent")) {
					silent = true;
					continue;
					
				}
				if(i.matches("^-A|--allResults$")){
					banAll = true;
					continue;
				}
				if(i.matches("^(?:-t|--temp)=(\\d+)(s|m|h|d|w)$")) {
					Matcher m = Pattern.compile("^(?:-t|--temp)=(\\d+)(m|h|d|w)$",Pattern.CASE_INSENSITIVE).matcher(i);
					if(!m.find())continue;
					long time = Long.parseLong(m.group(1));
					String unit = m.group(2).toLowerCase();
					switch(unit) {
					case "m":
						time = time*60*1000;
						break;
					case "h":
						time = time*60*60*1000;
						break;
					case "d":
						time = time*24*60*60*1000;
						break;
					case "w":
						time = time*7*24*60*60*1000;
						break;
					default:
						break;
					}
					tempTime = Instant.now().plusMillis(time).toEpochMilli();
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
						Utilities.reply(e.getMessage(), "I don't have permissions to ban!");	
						return;
					}
					int tbandays = bandays;
					String reason = String.join(" ", args);
					if(reason.split(args[0]).length > 1) {
						reason = reason.split(args[0])[1];
					} else reason = "[no reason specified]";
					String treason = reason;
					target.ban(a->{a.setDeleteMessageDays(tbandays);a.setReason("["+e.getMember().get().getUsername()+"#"+e.getMember().get().getDiscriminator()+" ("+e.getMember().get().getId().asString()+") ] "+treason);}).subscribe();
					if(tempTime > 0) {
						Main.db.getCollection("timed").insertOne(Document.parse("{\"type\":\"unban\",\"guild\":"+e.getGuild().block().getId().asString()+",\"target\":\""+target.getId().asString()+"\",\"due\":"+tempTime+"}"));
						ModLogger.logCase(e.getGuild().block(), ModLogger.newCase(target, e.getMember().get(), reason, Instant.ofEpochMilli(tempTime), CaseType.BAN, 0, e.getGuild().block()));
					} else {
						ModLogger.logCase(e.getGuild().block(), ModLogger.newCase(target, e.getMember().get(), reason, null, CaseType.BAN, 0, e.getGuild().block()));
					}
					if(!silent) {
						Utilities.reply(e.getMessage(), e.getMember().get().getMention() + " Banned "+target.getUsername());
						return;
					}
					return;
				}
				Utilities.reply(e.getMessage(), "That user didnt exist!");
			}
			List<Member> partialresult = ArgumentParser.toMemberByPartial(actualArgs.get(0), e.getGuild().block());	
			if(partialresult.size() > 1 && !banAll) {
				Utilities.reply(e.getMessage(), "There are more than one member that match that input!\n```\n"+
						String.join("\n", partialresult.stream().map(a->{return a.getUsername()+"#"+a.getDiscriminator();}).collect(Collectors.toList()))+"\n```");
				return;
			}
			if(partialresult.size() < 1) {
				Utilities.reply(e.getMessage(), "No members matched! Check your input and try again!");
				return;
			}
			StringBuilder output = new StringBuilder("");
			for(Member target : partialresult) {
				if(target.isHigher(e.getMember().get()).block()) {
					output.append("That user is above you!\n");
					continue;
				}
				if(target.isHigher(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block()).block()) {
					output.append("That user is above the bot!\n");
					continue;
				}
				if(!(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block().getBasePermissions().block().contains(Permission.BAN_MEMBERS))) {
					output.append("I don't have permissions to ban!\n");	
					break;
				}
				int tbandays = bandays;
				String reason = String.join(" ", args);
				if(reason.split(args[0]).length > 1) {
					reason = reason.split(args[0])[1];
				} else reason = "[no reason specified]";
				String treason = reason;
				target.ban(a->{a.setDeleteMessageDays(tbandays);a.setReason("["+e.getMember().get().getUsername()+"#"+e.getMember().get().getDiscriminator()+" ("+e.getMember().get().getId().asString()+") ] "+treason);}).subscribe();
				if(tempTime > 0) {
					Main.db.getCollection("timed").insertOne(Document.parse("{\"type\":\"unban\",\"guild\":"+e.getGuild().block().getId().asString()+",\"target\":\""+target.getId().asString()+"\",\"due\":"+tempTime+"}"));
					ModLogger.logCase(e.getGuild().block(), ModLogger.newCase(target, e.getMember().get(), reason, Instant.ofEpochMilli(tempTime), CaseType.BAN, 0, e.getGuild().block()));
				} else {
					ModLogger.logCase(e.getGuild().block(), ModLogger.newCase(target, e.getMember().get(), reason, null, CaseType.BAN, 0, e.getGuild().block()));
				}
				if(!silent) {
					output.append(e.getMember().get().getMention() + " Banned "+target.getUsername()+"\n");
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
