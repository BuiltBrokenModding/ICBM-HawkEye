package com.builtbroken.icbm.hawkeye;

import calclavia.api.icbm.explosion.ExplosionEvent.PreExplosionEvent;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.IEventListener;

public class ForgeEventHandler implements IEventListener
{
	@Override
	public void invoke(Event paramEvent)
	{
		if(paramEvent instanceof PreExplosionEvent)
		{
			PluginICBM.instance().preExplosion((PreExplosionEvent) paramEvent);
		}		
	}

}
