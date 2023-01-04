package org.zeith.expequiv.compat.js;

import javax.script.ScriptEngine;
import java.util.HashMap;
import java.util.Map;

public record ExtraJSContext(Map<String, Object> extraElements)
{
	public ExtraJSContext put(String key, Object value)
	{
		extraElements.put(key, value);
		return this;
	}
	
	public static ExtraJSContext create()
	{
		return new ExtraJSContext(new HashMap<>());
	}
	
	public void apply(ScriptEngine engine)
	{
		for(var e : extraElements.entrySet())
			engine.put(e.getKey(), e.getValue());
	}
}