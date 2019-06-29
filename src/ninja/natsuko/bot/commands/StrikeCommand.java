package ninja.natsuko.bot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.moderation.ModLogger;
import ninja.natsuko.bot.util.ArgumentParser;
import ninja.natsuko.bot.util.Utilities;

public class StrikeCommand extends Command {

	public StrikeCommand() {
		super("strike", "Strike a member. Usage: n;strike <Mention, ID or query> [-A/--allResults|-s/--silent] [reason]");
	}

	
	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		List<String> actualArgs = ArgumentParser.toArgs(String.join(" ", args));
		if(actualArgs.size() == 0) {
			Utilities.reply(e.getMessage(), this.description);
			return;
		}
		boolean kickAll = false;
		boolean silent = false;
		if(!e.getMember().isPresent()) return;
		if(Utilities.userIsModerator(e.getMember().get())) {
			for(String i : actualArgs) {
				if(i.matches("-s|--silent")) {
					silent = true;
					continue;
				}
				if(i.matches("^-A|--allResults$")){
					kickAll = true;
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
					if(!(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block().getBasePermissions().block().contains(Permission.KICK_MEMBERS))) {
						Utilities.reply(e.getMessage(),"I don't have permissions to kick!");	
						return;
					}
					if(!(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block().getBasePermissions().block().contains(Permission.BAN_MEMBERS))) {
						Utilities.reply(e.getMessage(),"I don't have permissions to ban!");	
						return;
					}
					String reason = String.join(" ", args);
					if(reason.split(args[0]).length > 1) {
						reason = reason.split(args[0])[1];
					} else reason = "[no reason specified]";
					Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first();
					List<Document> strikes = guildoc.get("strikes", new ArrayList<>());
					List<Document> temp = strikes.stream().filter(a->a.getLong("id") == target.getId().asLong()).collect(Collectors.toList());
					Document userStrikes;
					if(temp.size() < 1) {
						userStrikes = Document.parse("{\"id\":"+target.getId().asString()+",\"strikes\":0}");
					} else
					userStrikes = temp.get(0);
					if(userStrikes == null) {
						userStrikes = Document.parse("{\"id\":"+target.getId().asLong()+",\"strikes\":1}");
						strikes.add(userStrikes);
					}
					else {
						int i = strikes.indexOf(userStrikes);
						userStrikes.put("strikes", userStrikes.getInteger("strikes")+1);
						strikes.set(i, userStrikes);
					}
					guildoc.put("strikes", strikes);
					Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()), guildoc);
					Utilities.processStrike(target,userStrikes.getInteger("strikes"));
					ModLogger.logCase(e.getGuild().block(), ModLogger.newCase(target, e.getMember().get(), reason, null, CaseType.STRIKE, 1, e.getGuild().block()));
					if(!silent) {
						Utilities.reply(e.getMessage(), e.getMember().get().getMention() + " Struck "+target.getUsername());
						return;
					}
					return;
				}
				
			}
			List<Member> partialresult = ArgumentParser.toMemberByPartial(actualArgs.get(0), e.getGuild().block());	
			if(partialresult.size() > 1 && !kickAll) {
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
					output.append("That user is above you!\n");
					continue;
				}
				if(target.isHigher(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block()).block()) {
					output.append("That user is above the bot!\n");
					continue;
				}
				if(!(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block().getBasePermissions().block().contains(Permission.KICK_MEMBERS))) {
					output.append("I don't have permissions to kick!\n");	
					break;
				}
				if(!(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block().getBasePermissions().block().contains(Permission.BAN_MEMBERS))) {
					output.append("I don't have permissions to ban!\n");	
					break;
				}
				String reason = String.join(" ", args);
				if(reason.split(args[0]).length > 1) {
					reason = reason.split(args[0])[1];
				} else reason = "[no reason specified]";
				Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first();
				List<Document> strikes = guildoc.get("strikes", new ArrayList<>());
				List<Document> temp = strikes.stream().filter(a->a.getLong("id") == target.getId().asLong()).collect(Collectors.toList());
				Document userStrikes;
				if(temp.size() < 1) {
					userStrikes = Document.parse("{\"id\":"+target.getId().asString()+",\"strikes\":0}");
				} else
				userStrikes = temp.get(0);
				if(userStrikes == null) {
					userStrikes = Document.parse("{\"id\":"+target.getId().asLong()+",\"strikes\":1}");
					strikes.add(userStrikes);
				}
				else {
					int i = strikes.indexOf(userStrikes);
					userStrikes.put("strikes", userStrikes.getInteger("strikes")+1);
					strikes.set(i, userStrikes);
				}
				guildoc.put("strikes", strikes);
				Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(e.getGuild().block()), guildoc);
				Utilities.processStrike(target,userStrikes.getInteger("strikes"));
				ModLogger.logCase(e.getGuild().block(), ModLogger.newCase(target, e.getMember().get(), reason, null, CaseType.STRIKE, 1, e.getGuild().block()));
				if(!silent) {
					output.append(e.getMember().get().getMention() + " Struck "+target.getUsername()+"\n");
					continue;
				}
			}
			Utilities.reply(e.getMessage(), output.toString());
			return;
		}
		Utilities.reply(e.getMessage(), "You dont have permissions to kick!");
		return;
	}
	
}
