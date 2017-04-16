package com.builtbroken.icbm.hawkeye;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldInfo;

/**
 * Utility to get info out of the world class using reflection
 * 
 * @author Robert Seifert
 * 
 */
public class MinecraftAccessor
{
	public static Entity getExploder(Explosion ex)
	{
		Field f = null;
		try
		{
			try
			{
				f = ex.getClass().getField("exploder");
			}
			catch (NoSuchFieldException e1)
			{
				f = ex.getClass().getField("field_77283_e");

			}
			f.setAccessible(true);
			return (Entity) f.get(ex);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/** Gets the world provider from the world using reflection */
	public static WorldProvider getProvider(World world)
	{
		Field f = null;
		try
		{
			try
			{
				f = world.getClass().getField("provider");
			}
			catch (NoSuchFieldException e1)
			{
				f = world.getClass().getField("field_73011_w");

			}
			f.setAccessible(true);
			return (WorldProvider) f.get(world);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/** Gets the dim id from the world using reflection */
	public static int getDimID(World world)
	{
		try
		{
			WorldProvider provider = getProvider(world);
			if (provider != null)
			{
				Field f = null;
				try
				{
					f = provider.getClass().getField("dimensionId");
				}
				catch (NoSuchFieldException e1)
				{
					f = provider.getClass().getField("field_76574_g");
				}
				f.setAccessible(true);
				return f.getInt(provider);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	/** Gets the dim id from the world using reflection */
	public static String getDimName(World world)
	{
		try
		{
			WorldProvider provider = getProvider(world);
			if (provider != null)
			{
				Method m = null;
				try
				{
					m = provider.getClass().getMethod("getDimensionName");
				}
				catch (NoSuchMethodException e1)
				{
					m = provider.getClass().getMethod("func_80007_l");
				}
				if (m != null)
					return (String) m.invoke(provider);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/** Gets the WorldInfo from the world using reflection */
	public static WorldInfo getWorldInfo(net.minecraft.world.World world)
	{
		try
		{
			Method m = null;
			try
			{
				m = world.getClass().getMethod("getWorldInfo");
			}
			catch (NoSuchMethodException e1)
			{
				m = world.getClass().getMethod("func_72912_H");
			}
			if (m != null)
				return (WorldInfo) m.invoke(world);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/** Gets the WorldName from the world using reflection */
	public static String getWorldName(World world)
	{
		try
		{
			WorldInfo info = getWorldInfo(world);
			if (info != null)
			{
				Method m = ReflectionHelper.getMethodWithAlt(info.getClass(), "getWorldName", "func_76065_j");
				if (m != null)
					return (String) m.invoke(info);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/** Gets the WorldName from the world using reflection */
	public static String getEntityName(Entity entity)
	{
		try
		{
			Method m = ReflectionHelper.getMethodWithAlt(entity.getClass(), "getCommandSenderName", "func_70005_c_");
			if (m != null)
				return (String) m.invoke(entity);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
