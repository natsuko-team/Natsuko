package ninja.natsuko.bot.moderation;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import ninja.natsuko.bot.Main;

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
	
	public Date date;
	
	// this should be overridden with the reason, if any
	public String reason = "[No reason specified]";
	
	// only set if CaseType = TEMPMUTE | TEMPBAN
	public Date expiryDate;
	
	// must be set if CaseType = STRIKE | UNSTRIKE
	public int strikes;
	
	/**
	 * Generates a moderation log from this case to be sent as a Discord message.
	 * @return The formatted mod log string.
	 */
	public String toModLog() {
		String time = new SimpleDateFormat("HH:mm:ss").format(this.date);
		
		String[] emojiForCase = this.caseToEmoji.get(this.type);
		if (emojiForCase == null) { // should never get here but we'll add a case
			emojiForCase = new String[] { ":question:", null };
		}
		
		String emoji = emojiForCase[0];
		
		// check if we can use the custom emoji
		if (this.guild.getMemberById(Main.client.getSelfId().get()).block()
				.getBasePermissions().block().contains(Permission.USE_EXTERNAL_EMOJIS)
				&& emojiForCase[1] != null) {
			emoji = emojiForCase[1];
		}
		
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
		
		String formatBase = "`[ %s ]` %s (`%s`) %s %s (`%s`)\n`[  %s  ]` %s";
		String temporary = "\n`[  %s ]` %s";
		String finalStr = String.format(formatBase, time, emoji,
				moderator, this.moderatorUser.getId().asString(), action, target,
				this.targetUser.getId().asString(), "Reason", this.reason);
		
		if (this.type == CaseType.TEMPBAN || this.type == CaseType.TEMPMUTE) {
			finalStr += String.format(temporary, "Expires",
					DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format((TemporalAccessor) this.expiryDate));
		}
		
		return finalStr;
	}
}