package ninja.natsuko.bot.commands;

import java.util.List;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import ninja.natsuko.bot.util.ArgumentParser;
import ninja.natsuko.bot.util.Utilities;

public class KickCommand extends Command {

	public KickCommand() {
		super("kick", "Kick a user from the server");
	}

	
	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		List<String> actualArgs = ArgumentParser.toArgs(String.join(" ", args));
		if(!e.getMember().isPresent()) return;
		if(Utilities.userIsModerator(e.getMember().get())) {
			if(Utilities.isNumbers(actualArgs.get(0).replaceAll("[<@!>]", ""))) {
				Member target = ArgumentParser.toMemberByID(actualArgs.get(0).replaceAll("[<!@>]", ""), e.getGuild().block());
				if(target.isHigher(e.getMember().get()).block()) {
					Utilities.reply(e.getMessage(), "That user is above you!");
					return;
				}
				if(target.isHigher(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block()).block()) {
					Utilities.reply(e.getMessage(), "That user is above the bot!");
					return;
				}
				target.kick("["+e.getMember().get().getUsername()+"#"+e.getMember().get().getDiscriminator()+" ("+e.getMember().get().getId().asString()+") ] "+String.join(" ", args).substring(args[0].length()+1)).subscribe();
				//TODO properly modlog it @lewistehminerz
				if(!actualArgs.get(1).matches("-s|--silent")) {
					Utilities.reply(e.getMessage(), e.getMember().get().getMention() + " Kicked "+target.getUsername());
					return;
				}
				return;
			}
			List<Member> partialresult = ArgumentParser.toMemberByPartial(actualArgs.get(0), e.getGuild().block());	
			if(partialresult.size() > 1) {
				Utilities.reply(e.getMessage(), "There are more than one member that match that input!\n```\n"+
						String.join("\n", partialresult.stream().map(a->{return a.getUsername()+"#"+a.getDiscriminator();}).collect(Collectors.toList()))+"\n```");
				return;
			}
			Member target = partialresult.get(0);
			if(target.isHigher(e.getMember().get()).block()) {
				Utilities.reply(e.getMessage(), "That user is above you!");
				return;
			}
			if(target.isHigher(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block()).block()) {
				Utilities.reply(e.getMessage(), "That user is above the bot!");
				return;
			}
			if(!(e.getGuild().block().getMemberById(e.getClient().getSelfId().get()).block().getBasePermissions().block().contains(Permission.KICK_MEMBERS))) {
				Utilities.reply(e.getMessage(), "I don't have permissions to kick!");	
				return;
			}
			target.kick("["+e.getMember().get().getUsername()+"#"+e.getMember().get().getDiscriminator()+" ("+e.getMember().get().getId().asString()+") ] "+String.join(" ", args).substring(args[0].length()+1)).subscribe();
			//TODO properly modlog it @lewistehminerz
			if(!actualArgs.get(1).matches("-s|--silent")) {
				Utilities.reply(e.getMessage(), e.getMember().get().getMention() + " Kicked "+target.getUsername());
				return;
			}
			return;
		}
		Utilities.reply(e.getMessage(), "You dont have permissions to kick!");
		return;
	}
	
}
