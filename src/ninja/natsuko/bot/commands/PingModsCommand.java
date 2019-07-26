package ninja.natsuko.bot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.presence.Status;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.util.Utilities;

public class PingModsCommand extends Command {

	public PingModsCommand() {
		super("pingmods","Ping an active moderator. Usage: n;pingmods [reason]");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		Long modRole = Main.db.getCollection("guilds").find(org.bson.Document.parse("")).first().getLong("modRole");
		if(modRole == null) {
			Utilities.reply(e.getMessage(), "⚠ The `Moderators` role has not been set! Please set it with <NYI COMMAND>"); //TODO implement modlog setting
			return;
		}
		String modId = e.getGuild().block().getMembers().filter(m->{return m.getRoleIds().contains(Snowflake.of(modRole)) && m.getPresence().block().getStatus().equals(Status.ONLINE);}).blockFirst().getId().asString();
		Utilities.reply(e.getMessage(), "Mod Autoping: <@"+modId+"> \n"
				+ "**"+((String.join("args", " ").length()>1)?String.join("args", " "):"No reason provided.")+"**\n"
				+ "From: **"+e.getMember().get().getUsername()+"** ("+e.getMember().get().getId().asString()+")");
		Main.pingmodsAwaitingConfirm.put(e.getGuild().block().getId().asLong(),e.getMessage().getChannel().block().getId().asLong());
		return;
	}

}
