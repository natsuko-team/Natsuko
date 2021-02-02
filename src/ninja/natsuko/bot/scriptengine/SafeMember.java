package ninja.natsuko.bot.scriptengine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.exceptions.NotConfiguredException;
import ninja.natsuko.bot.moderation.Case.CaseType;
import ninja.natsuko.bot.moderation.ModLogger;
import ninja.natsuko.bot.util.Utilities;

public class SafeMember {

	private Member member;
	
	public SafeMember(Member member) {
		this.member = member;
	}

	public void ban(String reason,int deleteDays, int expire) {
		this.member.ban(a->{a.setReason("[SCRIPT]"+reason);a.setDeleteMessageDays(deleteDays);}).block();
		ModLogger.logCase(this.member.getGuild().block(), ModLogger.newCase(this.member, Main.client.getSelf().block(), "[SCRIPT]"+reason, Instant.ofEpochMilli(expire), CaseType.BAN, 0, this.member.getGuild().block()));
		if(expire > 0) {
			Main.db.getCollection("timed").insertOne(Document.parse("{\"type\":\"unban\",\"guild\":"+this.member.getGuild().block().getId().toString()+",\"target\":\""+this.member.getId().toString()+"\",\"due\":"+expire+"}"));
		}
	}
	
	public void kick(String reason) {
		this.member.kick("[SCRIPT]"+reason).block();
		ModLogger.logCase(this.member.getGuild().block(), ModLogger.newCase(this.member, Main.client.getSelf().block(), "[SCRIPT]"+reason, null, CaseType.KICK, 0, this.member.getGuild().block()));

	}
	
	public void strike(String reason, int amount, int expire) {
		Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(this.member.getGuild().block())).first();
		List<Document> strikes = guildoc.get("strikes", new ArrayList<>());
		List<Document> temp = strikes.stream().filter(a->a.getLong("id") == this.member.getId().asLong()).collect(Collectors.toList());
		Document userStrikes;
		if(temp.size() < 1) {
			userStrikes = Document.parse("{\"id\":"+this.member.getId().asLong()+",\"strikes\":"+amount+"}");
			strikes.add(userStrikes);
		} else {
			userStrikes = temp.get(0);
			int i = strikes.indexOf(userStrikes);
			userStrikes.put("strikes", userStrikes.getInteger("strikes")+amount);
			strikes.set(i, userStrikes);
		}
		guildoc.put("strikes", strikes);
		Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(this.member.getGuild().block()), guildoc);
		Utilities.processStrike(this.member,userStrikes.getInteger("strikes"));
		if(expire > 0) {
			Main.db.getCollection("timed").insertOne(Document.parse("{\"type\":\"unstrike\",\"guild\":"+this.member.getGuild().block().getId().toString()+",\"target\":\""+this.member.getId().toString()+"\",\"due\":"+expire+"}"));
		}
		ModLogger.logCase(this.member.getGuild().block(), ModLogger.newCase(this.member, Main.client.getSelf().block(), "[SCRIPT]"+reason, Instant.ofEpochMilli(expire), CaseType.STRIKE, amount, this.member.getGuild().block()));
	}
	
	public void unstrike(String reason, int amount) {
		Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(this.member.getGuild().block())).first();
		List<Document> strikes = guildoc.get("strikes", new ArrayList<>());
		List<Document> temp = strikes.stream().filter(a->a.getLong("id") == this.member.getId().asLong()).collect(Collectors.toList());
		Document userStrikes;
		if(temp.size() < 1) {
			userStrikes = Document.parse("{\"id\":"+this.member.getId().asLong()+",\"strikes\":"+amount+"}");
			strikes.add(userStrikes);
		} else {
			userStrikes = temp.get(0);
			int i = strikes.indexOf(userStrikes);
			userStrikes.put("strikes", userStrikes.getInteger("strikes")-amount);
			strikes.set(i, userStrikes);
		}
		guildoc.put("strikes", strikes);
		Main.db.getCollection("guilds").replaceOne(Utilities.guildToFindDoc(this.member.getGuild().block()), guildoc);
		Utilities.processStrike(this.member,userStrikes.getInteger("strikes"));
		ModLogger.logCase(this.member.getGuild().block(), ModLogger.newCase(this.member, Main.client.getSelf().block(), "[SCRIPT]"+reason, null, CaseType.UNSTRIKE, amount, this.member.getGuild().block()));
	}
	
	public void mute(String reason,int expire) throws NotConfiguredException {
		Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(this.member.getGuild().block())).first();
		Map<String,Object> opts = guildoc.get("options", new HashMap<>());
		if(!opts.containsKey("mutedrole")) {
			throw new NotConfiguredException("Muted Role has not been configured.");
		}
		this.member.addRole(Snowflake.of(opts.get("mutedrole").toString())).subscribe();	
		if(expire > 0) {
			Main.db.getCollection("timed").insertOne(Document.parse("{\"type\":\"unmute\",\"guild\":"+this.member.getGuild().block().getId().toString()+",\"target\":\""+this.member.getId().toString()+"\",\"due\":"+expire+"}"));
		}
		ModLogger.logCase(this.member.getGuild().block(), ModLogger.newCase(this.member, Main.client.getSelf().block(), "[SCRIPT]"+reason, Instant.ofEpochMilli(expire), CaseType.MUTE, 0, this.member.getGuild().block()));
	}
	
	public void unmute(String reason) throws NotConfiguredException {
		Document guildoc = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(this.member.getGuild().block())).first();
		Map<String,Object> opts = guildoc.get("options", new HashMap<>());
		if(!opts.containsKey("mutedrole")) {
			throw new NotConfiguredException("Muted Role has not been configured.");
		}
		this.member.removeRole(Snowflake.of(opts.get("mutedrole").toString())).subscribe();	
		ModLogger.logCase(this.member.getGuild().block(), ModLogger.newCase(this.member, Main.client.getSelf().block(), "[SCRIPT]"+reason, null, CaseType.MUTE, 0, this.member.getGuild().block()));
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
