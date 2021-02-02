package ninja.natsuko.bot.scriptengine;

import java.time.Instant;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class SafeMessage {

	private Message message;
	
	public SafeMessage(Message message) {
		this.message = message;
	}
	
	public String getContent() {
		if(this.message.getContent()!=null) {
			return this.message.getContent();
		}
		return null;
	}
	
	public Instant getTimestamp() {
		return this.message.getTimestamp();
	}
	
	public Snowflake getId() {
		return this.message.getId();
	}
	
	public SafeMember getAuthor() {
		return new SafeMember(this.message.getAuthorAsMember().block());
	}
	
	public SafeChannel getChannel() {
		return new SafeChannel((TextChannel) this.message.getChannel().block());
	}
	
	public SafeGuild getGuild() {
		return new SafeGuild(this.message.getGuild().block());
	}
	
	public boolean mentionsEveryone() {
		return this.message.mentionsEveryone();
	}
	
	public void delete() {
		this.message.delete().subscribe();
	}
	
	public SafeMessage edit(String edit) {
		this.message = this.message.edit(e->e.setContent(edit)).block();
		return this;
	}

}
