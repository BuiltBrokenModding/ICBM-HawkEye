package com.builtbroken.icbm.hawkeye;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

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
            try
            {
            	ForgeEventHandler.load();
            	logger().info("Loaded event handler");
            }
            catch(Exception e)
            {
            	logger().info("Error: Failed to invoke EventHandler");
            	e.printStackTrace();
            }          
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
	
	public static void onEvent(World world, double x, double y, double z, String player, String exName)
	{
		HawkEyeAPI.addCustomEntry(instance(), "blast", player, new Location(world, x, y, z), exName);
	}
}

