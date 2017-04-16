package com.builtbroken.icbm.hawkeye;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

import calclavia.api.icbm.explosion.ExplosionEvent;
import calclavia.api.icbm.explosion.ExplosionEvent.PreExplosionEvent;

public class ForgeEventHandler implements IEventListener
{
	@Override
	public void invoke(Event paramEvent)
	{
		try
		{
			if(paramEvent instanceof ExplosionEvent)
			{
				System.out.println("Received pre-explosion event");
				preExplosion((ExplosionEvent)paramEvent);
			}		
		}
		catch(Throwable t)
		{
			System.out.println("Unexpected error handling Event");
			t.printStackTrace();
		}
	}
	
	public static void load() throws Exception
	{  		
  		//Get event bus
		Field field = MinecraftForge.class.getDeclaredField("EVENT_BUS");
		field.setAccessible(true);
		EventBus bus = (EventBus) field.get(null);
		
		//Get bus ID	
		field = bus.getClass().getDeclaredField("busID");
		field.setAccessible(true);
		int id = field.getInt(bus);	
		
		//Get listeners
		field = Event.class.getDeclaredField("listeners");
		field.setAccessible(true);		
		ListenerList list = (ListenerList) field.get(null);
		
		//Register event listener
		list.register(id, EventPriority.NORMAL, new ForgeEventHandler());
	}	


	public void preExplosion(ExplosionEvent event) 
	{		
		if(event instanceof PreExplosionEvent)
		{
			Explosion ex = event.explosion;
			if(ex != null)
			{		
				Entity entity = MinecraftAccessor.getExploder(ex);				
				if(entity instanceof EntityPlayer)
				{					
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
							// IF all fails which it normally does for bukkit world try with the worldName
							world = Bukkit.getWorld(worldName);
						}
					}
					
					try
					{
						Method method = PluginICBM.class.getDeclaredMethod("onEvent", null);
						method.invoke(null, world, event.x, event.y, event.z, entity.getCommandSenderName(), event.iExplosion.toString());
						System.out.println("Fired explosion event");
					}
					catch(Exception e)
					{
						System.out.println("Failed to invoke event method");
						e.printStackTrace();
					}
					
					for(Object object : ex.affectedBlockPositions)
					{
						System.out.println(object);
					}					
				}
			}
		}		
	}
}
