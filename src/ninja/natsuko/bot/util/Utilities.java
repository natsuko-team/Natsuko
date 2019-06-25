package ninja.natsuko.bot.util;

import java.awt.Color;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;

public class Utilities {
	public static String longMilisToTime(long ms) {
        long time = ms / 1000;
        long seconds = time % 60;
        time /= 60;
        long minutes = time % 60;
        time /= 60;
        long hours = time % 24;
        time /= 24;
        long days = time;
        
        String strseconds = Long.toString(seconds).length() == 1 ? '0' + Long.toString(Math.round(Math.floor(seconds))): Long.toString(Math.round(Math.floor(seconds)));
        String strminutes = Long.toString(minutes).length() == 1 ? '0' + Long.toString(Math.round(Math.floor(minutes))) : Long.toString(Math.round(Math.floor(minutes)));
        String strhours = Long.toString(hours).length() == 1 ? '0' + Long.toString(Math.round(Math.floor(hours))) : Long.toString(Math.round(Math.floor(hours)));

        return days+(days == 1 ? " Day, " : " Days, ")+strhours+":"+strminutes+":"+strseconds;
    }
	
	 public static Color embedColor = new Color(252, 113, 20);
	
	
	// message helpers \\
	
	// returns message but blocks
	public static Message sendMessageBlocking(MessageChannel chan, Consumer<? super MessageCreateSpec> spec) {
		return chan.createMessage(spec).block();
	}
	
	//returns message but blocks AND string
	public static Message sendMessageBlocking(MessageChannel chan, String content) {
		return chan.createMessage(content).block();
	}
	
	// ignores message but async
	public static void sendMessage(MessageChannel chan, Consumer<? super MessageCreateSpec> spec) {
		chan.createMessage(spec).subscribe();
	}
	
	//ignores message but async AND string
	public static void sendMessage(MessageChannel chan, String content) {
		chan.createMessage(content).subscribe();
	}
	
	// reply helpers \\
	
	// returns message but blocks
	public static Message replyBlocking(Message m, Consumer<? super MessageCreateSpec> spec) {
		return m.getChannel().block().createMessage(spec).block();
	}

	// returns message but blocks and string
	public static Message replyBlocking(Message m, String content) {
		return m.getChannel().block().createMessage(content).block();
	}
	
	// ignores message but async
	public static void reply(Message m, Consumer<? super MessageCreateSpec> spec) {
		m.getChannel().block().createMessage(spec).subscribe();
	}
	
	// ignores message but async and string
	public static void reply(Message m, String content) {
		m.getChannel().block().createMessage(content).subscribe();
	}
	
	// extras \\
	
	// just shorthand
	public static MessageChannel getChannel(MessageCreateEvent e) {
		return e.getMessage().getChannel().block();
	}
}
