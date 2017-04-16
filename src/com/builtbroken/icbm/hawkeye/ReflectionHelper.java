package com.builtbroken.icbm.hawkeye;

import java.lang.reflect.Method;

/**
 * Basic methods for common reflection code
 * 
 * @author Robert Seifert
 * 
 */
public class ReflectionHelper
{
	/** Gets a method in the target object, NoSuchMethodException returns null instead of an error  */
	public static Method getMethod(Object target, String name, Class[] params)
	{
		return getMethod(target.getClass(), name, params, false);
	}

	/** Gets a method in the target object, NoSuchMethodException returns null instead of an error  */
	public static Method getDeclaredMethod(Object target, String name, Class[] params)
	{
		return getMethod(target.getClass(), name, params, true);
	}

	/** Gets a method in the target class, NoSuchMethodException returns null instead of an error */
	public static Method getMethod(Class target, String name, Class[] params, boolean declaired)
	{
		Method m = null;
		try
		{
			if (declaired)
			{
				m = target.getDeclaredMethod(name, params);
			}
			else
			{
				m = target.getMethod(name, params);
			}
		}
		catch (NoSuchMethodException ne)
		{
			// Suppress this error and just return null
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return m;
	}

	public static Method getMethodWithAlt(Class target, String name, String altName, Class... params)
	{
		return getMethodWithAlt(target, name, altName, params, false);
	}

	public static Method getDeclairedMethodWithAlt(Class target, String name, String altName, Class... params)
	{
		return getMethodWithAlt(target, name, altName, params, true);
	}

	/**
	 * Gets a method from the target class. If it failed to find the method using the first name it
	 * will try the altName.
	 */
	public static Method getMethodWithAlt(Class target, String name, String altName, Class[] params, boolean declaired)
	{
		Method m = getMethod(target, name, params, declaired);
		if (m == null)
			m = getMethod(target, altName, params, declaired);
		return m;
	}
}
