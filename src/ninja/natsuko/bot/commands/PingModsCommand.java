package ninja.natsuko.bot.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
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
		if(args.length == 0) {
			Utilities.reply(e.getMessage(), this.description);
		}
		Map<String,Object> opts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(e.getGuild().block())).first().get("options", new HashMap<>());
		Long modRole = (Long) opts.getOrDefault("modrole",null);
		if(modRole == null) {
			Utilities.reply(e.getMessage(), "The Moderators role hasnt been set! Set it with `n;config set modrole <mod role id or mention>`!");
			return;
		}
		List<Member> mods = e.getGuild().block().getMembers().filter(a->a.getRoleIds().contains(Snowflake.of(modRole))).filter(a->a.getPresence().block().getStatus().equals(Status.ONLINE)).collect(Collectors.toList()).block();
		Member mod = mods.get(0);
		Utilities.reply(e.getMessage(), "Mod Autoping: "+mod.getMention()+"\n**"+String.join(" ", args)+"**\nFrom: **"+e.getMember().get().getUsername()+"#"+e.getMember().get().getDiscriminator()+"** ("+e.getMember().get().getId().asString()+")");
	}

}
