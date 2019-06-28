package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.util.Utilities;

public class IdentifyCommand extends Command {

	public IdentifyCommand() {
		super("identify","Identify a user's ranking. Usage: n;identify <mention or id>");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		if(args.length == 0) {
			Utilities.reply(e.getMessage(), this.description);
		}
		if(Utilities.isNumbers(args[0].replaceAll("[<@!>]", ""))) {
			long userId = Long.parseLong(args[0].replaceAll("[<@!>]", ""));
			User user = e.getClient().getUserById(Snowflake.of(userId)).block();
			if(user==null) {
				Utilities.reply(e.getMessage(), "That user did not exist!");
				return;
			}
			
			if(Utilities.userIsStaff(user)) {
				Utilities.reply(e.getMessage(), user.getUsername() + " Is Natsuko Staff.");
				return;
			}
			Member member = e.getGuild().block().getMemberById(user.getId()).block();
			if(member == null){
				Utilities.reply(e.getMessage(), "That user has no special permissions.");
			}
			if(Utilities.userIsAdministrator(member)) {
				Utilities.reply(e.getMessage(), user.getUsername() + " Is a server administrator.");
				return;
			}
			if(Utilities.userIsModerator(member)) {
				Utilities.reply(e.getMessage(), user.getUsername() + " Is a server moderator.");
				return;
			}
			Utilities.reply(e.getMessage(), "That user has no special permissions.");
		}
	}

}
