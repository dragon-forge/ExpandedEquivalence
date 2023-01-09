package org.zeith.expequiv.js.wrappers;

public class JSClass
{
	public Class<?> forName(String str) throws ClassNotFoundException
	{
		return getClass().getClassLoader().loadClass(str);
	}
}