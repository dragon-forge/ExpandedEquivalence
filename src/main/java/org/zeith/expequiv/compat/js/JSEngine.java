package org.zeith.expequiv.compat.js;

import net.minecraftforge.fml.ModList;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.*;
import java.util.Objects;
import java.util.function.Predicate;

public class JSEngine
{
	private static final NashornScriptEngineFactory NASHORN_FACTORY = new NashornScriptEngineFactory();
	
	protected final JSSource source;
	protected ScriptEngine script;
	final Invocable scriptFunctions;
	protected final boolean isEnabled;
	protected final String failedCheck;
	
	JSEngine(JSSource source, ExtraJSContext ctx) throws ScriptException
	{
		this.source = source;
		this.scriptFunctions = (Invocable) (this.script = newEngine(ctx));
		
		source.ptrs().forEach(script::put);
		
		for(String condition : source.conditions())
		{
			if(!Boolean.parseBoolean(Objects.toString(script.eval(condition))))
			{
				isEnabled = false;
				this.failedCheck = condition;
				return;
			}
		}
		
		this.failedCheck = null;
		script.eval(source.parsedJS());
		isEnabled = true;
	}
	
	public Object callFunction(String name, Object... args) throws ScriptException, NoSuchMethodException
	{
		return scriptFunctions.invokeFunction(name, args);
	}
	
	public static ScriptEngine newEngine(ExtraJSContext jsctx)
	{
		ScriptEngine se = NASHORN_FACTORY.getScriptEngine();
		
		Predicate<String> isModLoaded = ModList.get()::isLoaded;
		se.put("isModLoaded", isModLoaded);
		
		jsctx.apply(se);
		
		return se;
	}
}