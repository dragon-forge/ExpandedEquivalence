package org.zeith.expequiv.js;

import net.minecraft.resources.ResourceLocation;

import javax.script.ScriptException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public record JSSource(ResourceLocation path, Set<String> conditions, String parsedJS, Map<String, Object> ptrs)
{
	public JSEngine execute(ExtraJSContext ctx) throws ScriptException
	{
		return new JSEngine(this, ctx);
	}
	
	public JSSource addClassPointer(Class<?> cls, String codeName)
	{
		ptrs.put(codeName, cls);
		return this;
	}
	
	public JSSource addInstancePointer(Object instance, String codeName)
	{
		ptrs.put(codeName, instance);
		return this;
	}
	
	public static JSSource create(ResourceLocation path, Stream<String> lines) throws ScriptException
	{
		Set<String> conditions = new HashSet<>();
		StringBuilder content = new StringBuilder();
		Map<String, String> defines = new HashMap<>();
		lines.sequential().forEach(ln ->
		{
			AtomicReference<String> tmp = new AtomicReference<>(ln);
			defines.forEach((src, dst) -> tmp.set(tmp.get().replace(src, dst)));
			ln = tmp.get();
			
			String trimmedLn = ln.stripLeading();
			
			final int hashTagLen = 1;
			if(trimmedLn.startsWith("//#"))
				trimmedLn = trimmedLn.substring(2);
			
			if(trimmedLn.startsWith("#require "))
			{
				String kv = trimmedLn.substring(8 + hashTagLen);
				while(kv.endsWith(";")) kv = kv.substring(0, kv.length() - 1);
				conditions.add(kv);
				ln = "// Processed: " + ln;
			}
			
			if(trimmedLn.startsWith("#define "))
			{
				String[] kv = trimmedLn.substring(7 + hashTagLen).split(" ", 2);
				if(kv.length == 2) defines.put(kv[0], kv[1]);
				ln = "// Processed: " + ln;
			}
			
			if(trimmedLn.startsWith("#import "))
			{
				String c = trimmedLn.substring(7 + hashTagLen);
				while(c.endsWith(";")) c = c.substring(0, c.length() - 1);
				ln = "var " + c.substring(c.lastIndexOf('.') + 1) + " = Java.type(\"" + c + "\");";
			}
			
			content.append(ln).append(System.lineSeparator());
		});
		
		return new JSSource(path, conditions, content.toString(), new HashMap<>());
	}
}