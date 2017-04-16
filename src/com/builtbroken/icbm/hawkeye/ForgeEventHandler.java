package com.builtbroken.icbm.hawkeye;

import icbm.explosion.entities.EntityExplosion;
import icbm.explosion.entities.EntityMissile;
import icbm.explosion.explosive.blast.Blast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.EventBus;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.IEventListener;
import net.minecraftforge.event.ListenerList;

import org.bukkit.Bukkit;
import org.bukkit.World;

import universalelectricity.api.vector.Vector3;
import calclavia.api.icbm.explosion.ExplosionEvent;
import calclavia.api.icbm.explosion.ExplosionEvent.ExplosionConstructionEvent;
import calclavia.api.icbm.explosion.ExplosionEvent.PreExplosionEvent;
import calclavia.api.icbm.explosion.IExplosion;

public class ForgeEventHandler implements IEventListener
{
	@Override
	public void invoke(Event paramEvent)
	{
		try
		{
			if (paramEvent instanceof ExplosionEvent)
			{
				preExplosion((ExplosionEvent) paramEvent);
			}
		}
		catch (Throwable t)
		{
			System.out.println("Unexpected error handling Event");
			t.printStackTrace();
		}
	}

	public static void load() throws Exception
	{
		// Get event bus
		Field field = MinecraftForge.class.getDeclaredField("EVENT_BUS");
		field.setAccessible(true);
		EventBus bus = (EventBus) field.get(null);

		// Get bus ID
		field = bus.getClass().getDeclaredField("busID");
		field.setAccessible(true);
		int id = field.getInt(bus);

		// Get listeners
		field = Event.class.getDeclaredField("listeners");
		field.setAccessible(true);
		ListenerList list = (ListenerList) field.get(null);

		// Register event listener
		list.register(id, EventPriority.NORMAL, new ForgeEventHandler());
	}

	public void preExplosion(ExplosionEvent event)
	{
		System.out.println("Received explosion event: " + event);
		System.out.println("\tEx: " + event.iExplosion);
		System.out.println("\tExplosion: " + event.explosion);
		if (event instanceof ExplosionConstructionEvent)
		{
			IExplosion ex = event.iExplosion;
			if (ex instanceof Blast)
			{
				Blast blast = (Blast) ex;
				Entity entity = MinecraftAccessor.getExploder(blast);
				System.out.println("Trigger Man: " + entity);
				int dim = MinecraftAccessor.getDimID(event.world);
				String dimName = MinecraftAccessor.getDimName(event.world);
				String worldName = MinecraftAccessor.getWorldName(event.world);

				// First try getting world by dim id
				World world = Bukkit.getWorld("DIM" + dim);
				if (world == null)
				{
					// Then try with dimName
					world = Bukkit.getWorld(dimName);
					if (world == null)
					{
						// IF all fails which it normally does for bukkit world try with the
						// worldName
						world = Bukkit.getWorld(worldName);
					}
				}
				
				String name = entity != null ? MinecraftAccessor.getEntityName(entity) : "unknown";
				if(entity instanceof EntityExplosion)
				{
					EntityExplosion exEntity = (EntityExplosion) entity;
					name = "unknown";
				}
				
				if(entity instanceof EntityMissile)
				{
					EntityMissile missile = (EntityMissile) entity;
					name = "unknown";
				}

				try
				{
					PluginICBM.onEvent(world, event.x, event.y, event.z, "Trigger", name, ex.toString());
					System.out.println("Fired explosion event");
				}
				catch (Exception e)
				{
					System.out.println("Failed to invoke event method");
					e.printStackTrace();
				}
				List<Vector3> list = null;
				try
				{
					Field field;
					try
					{
						field = Blast.class.getField("blownBlocks");
					}
					catch(Exception e)
					{
						field = Blast.class.getDeclaredField("blownBlocks");
					}
					field.setAccessible(true);
					list = (List<Vector3>) field.get(blast);
					
					for (Vector3 vec : list)
					{
						try
						{
							PluginICBM.onEvent(world, vec.x, vec.y, vec.z, "BlockBreak", name, ex.toString());
							System.out.println(vec);
						}
						catch (Exception e)
						{
							System.out.println("Failed to invoke event method");
							e.printStackTrace();
						}
					}
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}
