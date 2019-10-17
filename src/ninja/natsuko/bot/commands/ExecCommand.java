package ninja.natsuko.bot.commands;

import java.lang.management.ManagementFactory;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.bot.util.Utilities;

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
    String message = e.getMessage().get("echo No input").toString()                  
    String out = "";
    
    Process p = Runtime.getRuntime().exec("example.bat");

    BufferedReader in =
        new BufferedReader(new InputStreamReader(p.getInputStream()));
    String inputLine;
    while ((inputLine = in.readLine()) != null) {
        System.out.println(inputLine);
        out += inputLine;
    }
    in.close();
    if(out.length() > 1900){
      Utilities.reply(e.getMessage, spec -> {
        spec.addFile("output.txt",new ByteArrayInputStream(out.getBytes());)
      });
    }
    Utilities.reply(e.getMessage(),out);
		return;
	}
	
}
