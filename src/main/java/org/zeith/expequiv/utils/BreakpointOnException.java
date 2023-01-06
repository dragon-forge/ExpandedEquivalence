package org.zeith.expequiv.utils;

import net.minecraftforge.fml.loading.FMLEnvironment;
import org.openjdk.nashorn.api.scripting.ScriptUtils;
import org.zeith.expequiv.ExpandedEquivalence;

public class BreakpointOnException
{
	public static final IBreakPoint BREAKPOINT = BreakpointOnException::breakpoint;
	
	public static void breakpoint(Object... data)
	{
		// Never execute inside a production environment!
		if(FMLEnvironment.production) return;
		unwrapJS(data);
		long start = System.currentTimeMillis();
		
		// PUT BREAKPOINT BELOW THIS LINE
		new Throwable().printStackTrace(); // PUT BREAKPOINT ON THIS LINE
		// PUT BREAKPOINT ABOVE THIS LINE
		
		if(System.currentTimeMillis() - start < 50L)
		{
			ExpandedEquivalence.LOG.warn("Somebody forgot to enable a breakpoint...");
		}
	}
	
	private static void unwrapJS(Object... data)
	{
		for(int i = 0; i < data.length; i++)
		{
			data[i] = ScriptUtils.unwrap(data[i]);
			if(data[i] instanceof Throwable e) e.printStackTrace();
		}
	}
	
	@FunctionalInterface
	public interface IBreakPoint
	{
		void breakpoint(Object... data);
	}
}