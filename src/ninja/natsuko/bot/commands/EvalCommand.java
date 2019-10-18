package ninja.natsuko.bot.commands;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import discord4j.core.event.domain.message.MessageCreateEvent;
import ninja.natsuko.bot.util.Utilities;

@Invisible	
public class EvalCommand extends Command {

	public EvalCommand() {
		super("eval", "Evaluate Nashorn code. Usage: n;eval <ecma5 js>");
	}

	@Override
	public void execute(String[] args, MessageCreateEvent e) {
		if(!Utilities.userIsStaff(e.getMember().get())) {
			Utilities.reply(e.getMessage(), "You arent staff. GTFO.");
			return;
		}
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		engine.put("e", e);
		engine.put("args", args);
		
		try {
			Object output = engine.eval(String.join(" ", args));
			if(output == null) {
				Utilities.reply(e.getMessage(), "```[java.lang.Object] null```");
				return;
			}
			Utilities.reply(e.getMessage(), "```[ "+output.getClass().getTypeName()+" ] "+output+"```");
		} catch (Exception e1) {
			StringWriter string = new StringWriter();
			PrintWriter print = new PrintWriter(string);
			e1.printStackTrace(print);aaaaa
			String trace = string.toString();
			String usefultrace = String.join("\n",Arrays.asList(trace.split("\n")).stream().filter(a->{return !(a.contains("ninja.natsuko") || a.contains("in <eval> at line number"));}).collect(Collectors.toList()));
			Utilities.reply(e.getMessage(), spec ->{
				spec.setContent(":warning: An error has occurred!\n```"+usefultrace.substring(0,(int) Utilities.minmax(0,1000,usefultrace.length()))+"```");
				spec.addFile("eval-trace-"+Instant.now().toEpochMilli(), new ByteArrayInputStream(trace.getBytes()));
			});
			e1.printStackTrace();
		}
	}

}
