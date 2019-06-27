package ninja.natsuko.bot.scriptengine;

import java.time.Instant;

import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;

public class SafeMember {

	private Member member;
	
	public SafeMember(Member member) {
		this.member = member;
	}

	public void ban(String reason,int deleteDays) {
		this.member.ban(a->{a.setReason(reason);a.setDeleteMessageDays(deleteDays);}).block();
	}
	
	public void kick(String reason) {
		this.member.kick(reason).block();
	}
	
	public String getName() {
		return this.member.getUsername();
	}
	
	public String getNickname() {
		return this.member.getNickname().orElse(null);
	}
	
	public Snowflake getId() {
		return this.member.getId();
	}
	
	public boolean isBot() {
		return this.member.isBot();
	}
	
	public Instant getJoinTime() {
		return this.member.getJoinTime();
	}
	
	public Instant getRegisterDate() {
		return this.member.getId().getTimestamp();
	}
	
}
