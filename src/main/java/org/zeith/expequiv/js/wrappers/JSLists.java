package org.zeith.expequiv.js.wrappers;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JSLists
{
	public <T> List<T> arrayList()
	{
		return new ArrayList<>();
	}
	
	public <T> List<T> arrayList(Collection<T> c)
	{
		return new ArrayList<>(c);
	}
	
	public IntList intList()
	{
		return new IntArrayList();
	}
	
	public <T> Set<T> hashSet()
	{
		return new HashSet<>();
	}
	
	public <T> Set<T> hashSet(Collection<T> c)
	{
		return new HashSet<>(c);
	}
	
	public <K, V> Map<K, V> hashMap()
	{
		return new ConcurrentHashMap<>();
	}
	
	public <K, V> Map<K, V> hashMap(Map<K, V> m)
	{
		return new ConcurrentHashMap<>(m);
	}
	
	public <T> Stream<T> stream(T[] array)
	{
		return Arrays.stream(array);
	}
	
	public <T> Stream<T> stream(Collection<T> array)
	{
		return array.stream();
	}
	
	public <T> Stream<T> stream(Iterable<T> array)
	{
		if(array instanceof Collection) return stream((Collection<T>) array);
		else return StreamSupport.stream(array.spliterator(), false);
	}
	
	public boolean isArray(Object instance)
	{
		return instance != null && instance.getClass().isArray();
	}
}