package ninja.natsuko.bot.util;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import ninja.natsuko.bot.Main;

public class ErrorHandler {
	static Logger logger = (Logger)LoggerFactory.getLogger("ninja.natsuko.bot.Main");
	public static void handle(Throwable e,MessageCreateEvent event) {
		String id = RandomStringUtils.randomAlphabetic(10); //TODO random strings as id's suck, use word ids instead. ex: artful staple plastic contingency . easier for people to remember and type
		logger.error(id,e);
		
		StringWriter string = new StringWriter();
		PrintWriter printWriter = new PrintWriter(string);
		e.printStackTrace(printWriter);
		
		String trace = string.toString();
		if (event.getMessage().getContent().isPresent()) {
			if (event.getMessage().getContent().get().startsWith("n;")) {
				event.getMessage().getChannel().block().createMessage(":warning: An error has occurred. We recommend you join the support server. Make sure to include this ID with your support request: `" + id +  "`.");
			}
		}
		
		TextChannel chan = (TextChannel) Main.client.getChannelById(Snowflake.of(597818301183295596L)).block();
		chan.createMessage(a->{
			a.setContent("ID: `" + id + "`\n" +  
				"Guild: " + event.getGuild().block().getName() + " [" + event.getGuild().block().getId().toString() + "]\n" +
				"Message Content: " + (event.getMessage().getContent().isPresent() ? event.getMessage().getContent().get() : "N/A"));
			a.addFile("natsuko-error_"+id+".txt",new ByteArrayInputStream(trace.getBytes(StandardCharsets.UTF_8)));
		}).subscribe();
		
	}
}
