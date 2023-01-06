package org.zeith.expequiv.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EMCData
{
	private final Map<String, Object> map = new ConcurrentHashMap<>();
	
	public void setup(String key, Object value)
	{
		map.putIfAbsent(key, value);
	}
	
	public void set(String key, Object value)
	{
		map.put(key, value);
	}
	
	public Object get(String key)
	{
		return map.get(key);
	}
	
	public void mergeTo(EMCData other)
	{
		for(var e : map.entrySet())
		{
			other.set(e.getKey(), e.getValue());
		}
	}
}