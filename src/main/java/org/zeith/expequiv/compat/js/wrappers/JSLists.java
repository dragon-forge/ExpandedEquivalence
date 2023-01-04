package org.zeith.expequiv.compat.js.wrappers;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JSLists
{
	public static <T> List<T> arrayList()
	{
		return new ArrayList<>();
	}
	
	public static <T> List<T> arrayList(Collection<T> c)
	{
		return new ArrayList<>(c);
	}
	
	public static IntList intList()
	{
		return new IntArrayList();
	}
	
	public static <T> Set<T> hashSet()
	{
		return new HashSet<>();
	}
	
	public static <T> Set<T> hashSet(Collection<T> c)
	{
		return new HashSet<>(c);
	}
	
	public static <K, V> Map<K, V> hashMap() { return new ConcurrentHashMap<>(); }
	
	public static <K, V> Map<K, V> hashMap(Map<K, V> m) { return new ConcurrentHashMap<>(m); }
	
	public static <T> Stream<T> stream(T[] array) { return Arrays.stream(array); }
	
	public static <T> Stream<T> stream(Collection<T> array) { return array.stream(); }
	
	public static <T> Stream<T> stream(Iterable<T> array)
	{
		if(array instanceof Collection) return stream((Collection<T>) array);
		else return StreamSupport.stream(array.spliterator(), false);
	}
	
	public static boolean isArray(Object instance)
	{
		return instance != null && instance.getClass().isArray();
	}
}