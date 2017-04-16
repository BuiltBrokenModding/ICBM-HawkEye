package com.builtbroken.icbm.hawkeye;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import calclavia.api.icbm.explosion.ExplosionEvent.PreExplosionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.EventPriority;
import uk.co.oliwali.HawkEye.util.HawkEyeAPI;

public class PluginICBM extends JavaPlugin {

	public boolean usingHawkEye = false;
	public static PluginICBM instance;
	
	private PluginLogger logger;

	@Override
    public void onEnable() 
	{		
		logger().info("Enabled!");
		instance = this;
		//See if hawkeye is enabled
        Plugin dl = getServer().getPluginManager().getPlugin("HawkEye");
        if (dl != null)
        {
            this.usingHawkEye = true;
            logger().info("HawkEye plugin found");
            
           
            Field f = null;
    		Event event = new Event();
    		int id = 0;
    		try
    		{
    			 //Get bus ID for Event.class
    			f = MinecraftForge.EVENT_BUS.getClass().getDeclaredField("busID");
    			f.setAccessible(true);
    			id = f.getInt(MinecraftForge.EVENT_BUS);
    		}
    		catch (NoSuchFieldException e1)
    		{
    			logger().fine("Failed to get event bus ID defaulting to zero");
    		}
    		catch (Exception e1)
    		{
    			e1.printStackTrace();
    		}
    		
    		//Add out own listener to the Event.class's listener list
    		event.getListenerList().register(id, EventPriority.NORMAL, new ForgeEventHandler());
        }
        else
        {
        	 logger().info("HawkEye plugin not found");
        }
    }	


	@Override
	public void onDisable()
	{
		logger().info("Disabled!");
	}

	/** Logger used by the plugin, mainly just prefixes everything with the name */
	public PluginLogger logger()
	{
		if (logger == null)
		{
			logger = new PluginLogger(this);
			logger.setParent(getLogger());
		}
		return logger;
	}

	public static PluginICBM instance() {		
		return instance;
	}

	public void preExplosion(PreExplosionEvent event) 
	{
		if(event != null)
		{
			Explosion ex = event.explosion;
			if(ex != null)
			{		
				Entity entity = ex.exploder;				
				if(entity instanceof EntityPlayer)
				{					
					int dim = WorldUtility.getDimID(event.world);
					String dimName = WorldUtility.getDimName(event.world);
					String worldName = WorldUtility.getWorldName(event.world);

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
					
					HawkEyeAPI.addCustomEntry(this, "blast", ((EntityPlayer)entity).getCommandSenderName(), new Location(world, event.x, event.y, event.z), "" + event.iExplosion.toString());
					
					for(Object object : ex.affectedBlockPositions)
					{
						System.out.println(object);
					}					
				}
			}
		}		
	}
    
}

