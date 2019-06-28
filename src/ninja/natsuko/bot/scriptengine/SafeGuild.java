package ninja.natsuko.bot.scriptengine;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.exceptions.ChannelNotTextException;

public class SafeGuild {

	private Guild guild;
	
	public SafeGuild(Guild guild) {
		this.guild = guild;
	}
	
	public String getName() {
		return this.guild.getName();
	}
	
	public Snowflake getId() {
		return this.guild.getId();
	}
	
	public SafeChannel getChannelById(Snowflake chanid) throws ChannelNotTextException {
		Channel channel = this.guild.getChannelById(chanid).block();
		if(!(channel instanceof TextChannel)) throw new ChannelNotTextException();
		return new SafeChannel((TextChannel) channel);
	}

}
