package ninja.natsuko.bot.moderation;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;

public class Case {

	@SuppressWarnings("serial")
	private Map<CaseType, String[]> caseToEmoji = new HashMap<CaseType, String[]>() {{
		// added some comments explaining why i've picked said emoji
		
		// emoji[0] = non-custom
		// emoji[1] = custom [null forces a fallback]
		// punishments
		put(CaseType.STRIKE, new String[] { ":triangular_flag_on_post:", null }); // https://en.wikipedia.org/wiki/Penalty_card#Red_card
		put(CaseType.TEMPMUTE, new String[] { ":zipper_mouth:", null }); // zipper suggests that they can reopen it, so it's temp
		put(CaseType.MUTE, new String[] { ":no_mouth:", null }); // no mouth suggests perma
		put(CaseType.KICK, new String[] { ":boot:", null }); // https://en.wikipedia.org/wiki/Bart_vs._Australia#Plot
		/**
		 * 'the Parliament of Australia reveals that they want to give him the "booting"'
		 * ...
		 * 'which is a **kick** in the buttocks using a giant **boot**'
		 */
		put(CaseType.TEMPBAN, new String[] { ":hammer:", null }); // hammer those bad boys
		put(CaseType.BAN, new String[] { ":hammer:", null });
		
		// removing punishments
		put(CaseType.UNSTRIKE, new String[] { ":flag_white:", null }); // don't know for this one, white flag? it's like surrendering a strike
		put(CaseType.UNMUTE, new String[] { ":open_mouth:", null }); // mouth appears again!
		put(CaseType.UNBAN, new String[] { ":wrench:", null }); // idk for this one, so i just threw in a wrench because i think vortex uses this?
	}};
	
	public enum CaseType {
		// punishments
		STRIKE,
		TEMPMUTE,
		MUTE,
		KICK,
		TEMPBAN,
		BAN,
		
		// removing punishments
		UNSTRIKE,
		UNMUTE,
		UNBAN
	}
	
	public CaseType type;
	
	public User targetUser;
	public User moderatorUser;
	public Guild guild;
	
	public Instant date;
	
	public int id; //this is per guild
	
	// this should be overridden with the reason, if any
	public String reason = "[No reason specified]";
	
	// only set if CaseType = TEMPMUTE | TEMPBAN
	public Instant expiryDate;
	
	// must be set if CaseType = STRIKE | UNSTRIKE
	public int strikes = -1;
	
	public Case(User target,User moderator,Instant date, CaseType type, Instant expiryDate, int id) {
		this.targetUser = target;
		this.moderatorUser = moderator;
		this.date = date;
		this.type = type;
		this.expiryDate = expiryDate;
		this.id = id;
	}
	
	public Case(User target,User moderator,Instant date, CaseType type, Instant expiryDate,String reason, int id) {
		this.targetUser = target;
		this.moderatorUser = moderator;
		this.date = date;
		this.type = type;
		this.expiryDate = expiryDate;
		this.reason = reason;
		this.id = id;
	}
	
	public Case(User target,User moderator,Instant date, CaseType type, Instant expiryDate, int strikes, int id) {
		this.targetUser = target;
		this.moderatorUser = moderator;
		this.date = date;
		this.type = type;
		this.expiryDate = expiryDate;
		this.strikes = strikes;
		this.id = id;
	}
	
	public Case(User target,User moderator,Instant date, CaseType type, Instant expiryDate,String reason, int strikes, int id) {
		this.targetUser = target;
		this.moderatorUser = moderator;
		this.date = date;
		this.type = type;
		this.expiryDate = expiryDate;
		this.reason = reason;
		this.strikes = strikes;
		this.id = id;
	}
	
	/**
	 * Converts the case into document form for storing in the DB
	 * @return The case, in document form.
	 */
	public Document toDocument() {
		Document out = new Document();
		out.put("targetUser", this.targetUser.getId().asLong());
		out.put("moderatorUser", this.moderatorUser.getId().asLong());
		out.put("date", this.date.toEpochMilli());
		out.put("type", this.type.toString().toLowerCase());
		out.put("expiryDate", this.expiryDate);
		out.put("reason", this.reason);
		out.put("id", this.id);
		if(this.strikes == -1) out.put("strikes", this.strikes);
		return out;
	}
	
	/**
	 * Generates a moderation log from this case to be sent as a Discord message.
	 * @return The formatted mod log string.
	 */
	public String toModLog() {
		String time = new SimpleDateFormat("HH:mm:ss").format(Date.from(this.date));
		
		String[] emojiForCase = this.caseToEmoji.get(this.type);
		if (emojiForCase == null) { // should never get here but we'll add a case
			emojiForCase = new String[] { ":question:", null };
		}
		
		String emoji = emojiForCase[0];
		
		// check if we can use the custom emoji
		//disabled until we get custom emoji lol
		/*if (this.guild.getMemberById(Main.client.getSelfId().get()).block()
				.getBasePermissions().block().contains(Permission.USE_EXTERNAL_EMOJIS)
				&& emojiForCase[1] != null) {
			emoji = emojiForCase[1];
		}*/
		
		String moderator = this.moderatorUser.getUsername() + "#" + this.moderatorUser.getDiscriminator();
		
		String action = "";
		switch(this.type) {
			// punishments
			case STRIKE:
				action = "gave " + this.strikes + " strikes to";
				break;
			case TEMPMUTE:
				action = "temporarily muted";
				break;
			case MUTE:
				action = "muted";
				break;
			case KICK:
				action = "kicked";
				break;
			case TEMPBAN:
				action = "temporarily banned";
				break;
			case BAN:
				action = "banned";
				break;
			
			// removing punishments
			case UNSTRIKE:
				action = "removed " + this.strikes + " strikes from";
				break;
			case UNMUTE:
				action = "unmuted";
				break;
			case UNBAN:
				action = "unbanned";
				break;
				
			default: // should never get here but we'll add a case anyway
				action = "did something to";
				break;
		}
		
		String target = this.targetUser.getUsername() + "#" + this.targetUser.getDiscriminator();
		
		String formatBase = "`[ %s ]` %s %s (`%s`) %s %s (`%s`)\n`[  %s  ]` %s";
		String temporary = "\n`[  %s ]` %s";
		String finalStr = String.format(formatBase, time, emoji,
				moderator, this.moderatorUser.getId().asString(), action, target,
				this.targetUser.getId().asString(), this.reason);
		
		if (this.type == CaseType.TEMPBAN || this.type == CaseType.TEMPMUTE) {
			finalStr += String.format(temporary, "Expires",
					DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format((TemporalAccessor) Date.from(this.expiryDate)));
		}
		
		return finalStr;
	}
}