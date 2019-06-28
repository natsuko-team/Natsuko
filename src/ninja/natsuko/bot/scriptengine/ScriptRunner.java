package ninja.natsuko.bot.scriptengine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.ScriptException;

import delight.nashornsandbox.NashornSandbox;
import delight.nashornsandbox.NashornSandboxes;
import delight.nashornsandbox.exceptions.ScriptCPUAbuseException;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;
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
		this.sandbox.setExecutor(this.executor);
		reload();
	}
	
	public void reload() {
		this.loadedScripts = Main.db.getCollection("guilds").find(Utilities.guildToFindDoc(this.guild)).first().get("scripts",new ArrayList<String>());
		this.scriptsErrored = false;
	}
	
	public void run(Message message) {
		if(this.scriptsErrored) return;
		this.sandbox.allow(Snowflake.class);
		this.sandbox.inject("message", new SafeMessage(message));
		for(String i : this.loadedScripts) {
			try {
				this.sandbox.eval("function id(string){Packages.discord4j.core.object.util.Snowflake.of(string)}\n"+i);
			} catch (ScriptCPUAbuseException e) {
				Utilities.reply(message, "ERROR: A script exceeded the Memory or Time limit.\nPlease check your scripts for memory leaks or infinite loops.");
				this.scriptsErrored = true;
				return;
			} catch (ScriptException e) {
				Utilities.reply(message, "ERROR: A script threw an error:\n"+e.getMessage());
				this.scriptsErrored = true;
				return;
			}
		}
	}
}
