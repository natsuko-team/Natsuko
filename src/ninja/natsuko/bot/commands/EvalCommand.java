package ninja.natsuko.bot.commands;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
			Object output = engine.eval(String.join(" ", args)).toString();
			Utilities.reply(e.getMessage(), "```["+output.getClass().getTypeName()+"]"+output+"```");
		} catch (ScriptException e1) {
			StringWriter string = new StringWriter();
			PrintWriter print = new PrintWriter(string);
			e1.printStackTrace(print);
			String trace = string.toString();
			e.getMessage().getChannel().block().createMessage(":warning: An error has occurred!\n```"+trace+"```").subscribe();
			e1.printStackTrace();
		}
	}

}
