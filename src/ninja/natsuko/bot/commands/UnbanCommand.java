package ninja.natsuko.bot.commands;

import java.util.List;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Permission;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.moderation.ModLogger;
import ninja.natsuko.bot.util.ArgumentParser;
import ninja.natsuko.bot.util.Utilities;

public class UnbanCommand extends Command {

	public UnbanCommand() {
		super("unban", "Unban a user from the server. Usage: n;unban <ID or query> [-A/--allResults|-s/--silent] [reason]");
	}

	
	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		List<String> actualArgs = ArgumentParser.toArgs(String.join(" ", args));
		if(actualArgs.size() == 0) {
			Utilities.reply(e.getMessage(), this.description);
			return;
		}
		boolean unbanAll = false;
		boolean silent = false;
		if(!e.getMember().isPresent()) return;
		if(Utilities.userIsModerator(e.getMember().get())) {
			for(String i : actualArgs) {
				if(i.matches("-s|--silent")) {
					silent = true;
					continue;
				}
				if(i.matches("^-A|--allResults$")){
					unbanAll = true;
					continue;
				}
			}
			if(Utilities.isNumbers(actualArgs.get(0).replaceAll("[<@!>]", ""))) {
				User target = ArgumentParser.toUserByID(actualArgs.get(0).replaceAll("[<!@>]", ""));
				if(target != null) {
					if(!(e.getGuild().block().getMemberById(e.getClient().getSelfId()).block().getBasePermissions().block().contains(Permission.BAN_MEMBERS))) {
						Utilities.reply(e.getMessage(), "I don't have permissions to unban!");	
						return;
					}
					e.getGuild().block().unban(target.getId(),"").subscribe();
					
					String reason = String.join(" ", args);
					if(reason.split(args[0]).length > 1) {
						reason = reason.split(args[0])[1];
					} else reason = "[no reason specified]";
					ModLogger.logCase(e.getGuild().block(), ModLogger.newCase(target, e.getMember().get(), reason, null, CaseType.UNBAN, 0, e.getGuild().block()));
					if(!silent) {
						Utilities.reply(e.getMessage(), e.getMember().get().getMention() + " Unbanned "+target.getUsername());
						return;
					}
					return;
				}
				
			}
			List<User> partialresult = e.getGuild().block().getBans().toStream().map(b->b.getUser()).filter(a->(a.getUsername()+"#"+a.getDiscriminator()).contains(actualArgs.get(0))).collect(Collectors.toList());
			if(partialresult.size() > 1 && !unbanAll) {
				Utilities.reply(e.getMessage(), "There are more than one member that match that input!\n```\n"+
						String.join("\n", partialresult.stream().map(a->{return a.getUsername()+"#"+a.getDiscriminator();}).collect(Collectors.toList()))+"\n```");
				return;
			}
			if(partialresult.size() < 1) {
				Utilities.reply(e.getMessage(), "No members matched! Check your input and try again!");
			}
			StringBuilder output = new StringBuilder("");
			for(User target : partialresult) {
				if(!(e.getGuild().block().getMemberById(e.getClient().getSelfId()).block().getBasePermissions().block().contains(Permission.MANAGE_ROLES))) {
					output.append("I don't have permissions to Unban!");	
					break;
				}
				e.getGuild().block().unban(target.getId()).subscribe();
				String reason = String.join(" ", args);
				if(reason.split(args[0]).length > 1) {
					reason = reason.split(args[0])[1];
				} else reason = "[no reason specified]";
				ModLogger.logCase(e.getGuild().block(), ModLogger.newCase(target, e.getMember().get(), reason, null, CaseType.UNBAN, 0, e.getGuild().block()));
				if(!silent) {
					output.append(e.getMember().get().getMention() + " Unbanned "+target.getUsername());
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
