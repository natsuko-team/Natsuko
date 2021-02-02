package ninja.natsuko.bot.commands;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import ninja.natsuko.bot.util.ErrorHandler;
import ninja.natsuko.bot.util.Utilities;

@Invisible
public class ExecCommand extends Command {

	public ExecCommand() {
		super("exec", "Execute a command on the local system. Usage: n;exec <command...>");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		if(!Utilities.userIsStaff(e.getMember().get())) {
			Utilities.reply(e.getMessage(), "You arent staff. GTFO.");
			return;
		}
		String message = e.getMessage().getContent();   
		String command = message.substring(7);
		String out = "";
		Instant began = Instant.now();
		Message processing = Utilities.getChannel(e).createMessage("<a:loading:393852367751086090> Working...\nBegan at "+began.toEpochMilli()).block();
		try {
			Process p = Runtime.getRuntime().exec(command);	
			try(BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));){
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					out += inputLine + "\n";
				}
			}
		} catch (IOException e1) {
			ErrorHandler.handle(e1,e);
		}
		String finalout = out;
		if(out.length() > 1900){
			processing.delete().subscribe();
		    Utilities.reply(e.getMessage(), spec -> {
		    	spec.setContent("✅ Completed in " + ( Instant.now().toEpochMilli() - began.toEpochMilli() ) + "ms") ;
		    	spec.addFile("output.txt",new ByteArrayInputStream(finalout.getBytes()));
			});
			return;
		}
		processing.edit(spec -> {
	    	spec.setContent("✅ Completed in "+( Instant.now().toEpochMilli() - began.toEpochMilli() ) + "ms"
	    	+ "\n```" + finalout + "```");
		}).subscribe();
		return;
	}
	
}
