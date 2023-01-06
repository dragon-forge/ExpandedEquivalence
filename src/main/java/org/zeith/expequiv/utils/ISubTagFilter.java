package org.zeith.expequiv.utils;

import java.util.function.Predicate;

@FunctionalInterface
public interface ISubTagFilter
{
	boolean shouldBlock(String element, long emc, String tag);
	
	static Predicate<ISubTagFilter> checkBlockage(String element, long emc, String tag)
	{
		return filter -> filter.shouldBlock(element, emc, tag);
	}
}