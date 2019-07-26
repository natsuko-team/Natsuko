package ninja.natsuko.bot.scriptengine;


import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.util.Utilities;

public class SafeChannel {

	private TextChannel channel;
	
	public SafeChannel(TextChannel channel) {
		this.channel = channel;
	}
	
	public String getName() {
		return this.channel.getName();
	}
	
	public Snowflake getId() {
		return this.channel.getId();
	}

	public SafeGuild getGuild() {
		return new SafeGuild(this.channel.getGuild().block());
	}
	
	public SafeMessage send(String string) {
		return new SafeMessage(Utilities.sendMessageBlocking(this.channel, string));
	}
	
}
