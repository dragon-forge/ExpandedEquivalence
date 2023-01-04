package org.zeith.expequiv.compat.js.wrappers;

import java.lang.reflect.Field;

public class JSReflection
{
	public static Object getField(Object source, String name)
	{
		if(source instanceof Class)
		{
			try
			{
				for(Field f : ((Class<?>) source).getDeclaredFields())
				{
					if(f.getName().equals(name))
					{
						f.setAccessible(true);
						return f.get(null);
					}
				}
				
				for(Field f : ((Class<?>) source).getFields())
				{
					if(f.getName().equals(name))
					{
						f.setAccessible(true);
						return f.get(null);
					}
				}
				
				throw new NoSuchFieldException(name);
			} catch(NoSuchFieldException | IllegalAccessException e)
			{
				System.out.println(((Class<?>) source).getName() + " does not have " + name + " ???");
				e.printStackTrace();
			}
		} else if(source != null)
		{
			try
			{
				for(Field f : source.getClass().getDeclaredFields())
				{
					if(f.getName().equals(name))
					{
						f.setAccessible(true);
						return f.get(source);
					}
				}
				
				for(Field f : source.getClass().getFields())
				{
					if(f.getName().equals(name))
					{
						f.setAccessible(true);
						return f.get(source);
					}
				}
				
				throw new NoSuchFieldException(name);
			} catch(NoSuchFieldException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		
		return null;
	}
}