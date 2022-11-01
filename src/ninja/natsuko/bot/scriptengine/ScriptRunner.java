package ninja.natsuko.bot.scriptengine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.ScriptException;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.NashornSandboxes;
import delight.nashornsandbox.exceptions.ScriptCPUAbuseException;
import delight.nashornsandbox.exceptions.ScriptMemoryAbuseException;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import ninja.natsuko.bot.Main;
import ninja.natsuko.bot.util.Utilities;

public class ScriptRunner {

	List<String> loadedScripts = new ArrayList<>();
	
	ExecutorService executor = Executors.newFixedThreadPool(10);

	NashornSandbox sandbox = NashornSandboxes.create();
	
	boolean scriptsErrored = false;
	
	Guild guild;
	
	public ScriptRunner(Guild guild) {
		this.guild = guild;
		this.sandbox.setMaxCPUTime(60000);
		this.sandbox.setMaxMemory(30000000);
		this.sandbox.allow(Snowflake.class);
		this.sandbox.inject("util", new SafeUtils());
		this.sandbox.setExecutor(this.executor);
		reload();
	}
	
	public void reload() {
		this.loadedScripts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(this.guild)).first().get("scripts",new ArrayList<String>());
		this.scriptsErrored = false;
	}
	
	public void run(Message message) {
		if(this.scriptsErrored) return;
		ScriptContext ctx = new SimpleScriptContext();
                ctx.setAttribute("message", new SafeMessage(message), ScriptContext.ENGINE_SCOPE);
		for(String i : this.loadedScripts) {
			try {
				this.sandbox.eval("\n"+i, ctx);
			} catch (ScriptCPUAbuseException e) {
				Utilities.reply(message, "ERROR: A script exceeded the CPU Time limit.\nPlease check your scripts for infinite loops, or make them shorter.\nAll scripts have been disabled, You will not recieve a further message until a script is added, edited or deleted");
				this.scriptsErrored = true;
				return;
			} catch(ScriptMemoryAbuseException e) {
				Utilities.reply(message, "ERROR: A script exceeded the Memory Limit of 30 Megabytes.\nPlease check your scripts for memory leaks.\nAll scripts have been disabled, You will not recieve a further message until a script is added, edited or deleted");
				this.scriptsErrored = true;
				return;
			}
			catch (ScriptException e) {
				Utilities.reply(message, "ERROR: A script threw an error:\n```"+e.getMessage()+"```\nAll scripts have been disabled, You will not recieve a further message until a script is added, edited or deleted.");
				this.scriptsErrored = true;
				return;
			}
		}
	}
}
