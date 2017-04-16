package icbm.explosion.items;

import calclavia.api.icbm.explosion.ExplosionEvent.ExplosivePreDetonationEvent;
import calclavia.api.icbm.explosion.ExplosiveType;
import calclavia.lib.utility.LanguageUtility;
import icbm.Settings;
import icbm.core.prefab.item.ItemICBMElectrical;
import icbm.explosion.entities.EntityMissile;
import icbm.explosion.ex.Ex;
import icbm.explosion.explosive.ExplosiveRegistry;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventBus;
import universalelectricity.api.vector.Vector3;

public class ItemRocketLauncher
  extends ItemICBMElectrical
{
  private static final int ENERGY = 1000000;
  
  public ItemRocketLauncher(int par1)
  {
    super(par1, "rocketLauncher");
  }
  
  public EnumAction func_77661_b(ItemStack par1ItemStack)
  {
    return EnumAction.bow;
  }
  
  public ItemStack func_77659_a(ItemStack itemStack, World world, EntityPlayer player)
  {
    if (!world.field_72995_K) {
      if (getEnergy(itemStack) >= 1000000L) {
        for (int i = 0; i < player.field_71071_by.func_70302_i_(); i++)
        {
          ItemStack inventoryStack = player.field_71071_by.func_70301_a(i);
          if (inventoryStack != null) {
            if ((inventoryStack.func_77973_b() instanceof ItemMissile))
            {
              int haoMa = inventoryStack.func_77960_j();
              if ((ExplosiveRegistry.get(haoMa) instanceof Ex))
              {
                Ex daoDan = (Ex)ExplosiveRegistry.get(haoMa);
                
                ExplosionEvent.ExplosivePreDetonationEvent evt = new ExplosionEvent.ExplosivePreDetonationEvent(world, player.field_70165_t, player.field_70163_u, player.field_70161_v, ExplosiveType.AIR, ExplosiveRegistry.get(haoMa));
                MinecraftForge.EVENT_BUS.post(evt);
                if ((daoDan != null) && (!evt.isCanceled()))
                {
                  if ((daoDan.getTier() <= Settings.MAX_ROCKET_LAUCNHER_TIER) && (daoDan.isCruise()))
                  {
                    double dist = 5000.0D;
                    Vector3 diDian = Vector3.translate(new Vector3(player), new Vector3(0.0D, 0.5D, 0.0D));
                    Vector3 kan = new Vector3(player.func_70676_i(1.0F));
                    Vector3 start = Vector3.translate(diDian, Vector3.scale(kan, 1.1D));
                    Vector3 muBiao = Vector3.translate(diDian, Vector3.scale(kan, 100.0D));
                    
                    EntityMissile entityMissile = new EntityMissile(world, start, daoDan.getID(), -player.field_70177_z, -player.field_70125_A);
                    world.func_72838_d(entityMissile);
                    if (player.func_70093_af())
                    {
                      player.func_70078_a(entityMissile);
                      player.func_70095_a(false);
                    }
                    entityMissile.ignore(player);
                    entityMissile.launch(muBiao);
                    if (!player.field_71075_bZ.field_75098_d) {
                      player.field_71071_by.func_70299_a(i, null);
                    }
                    discharge(itemStack, 1000000L, true);
                    
                    return itemStack;
                  }
                }
                else {
                  player.func_70006_a(ChatMessageComponent.func_111066_d(LanguageUtility.getLocal("message.launcher.protected")));
                }
              }
            }
          }
        }
      }
    }
    return itemStack;
  }
  
  public long getVoltage(ItemStack itemStack)
  {
    return 1000L;
  }
  
  public long getEnergyCapacity(ItemStack theItem)
  {
    return 16000000L;
  }
  
  public void func_77624_a(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4)
  {
    String str = LanguageUtility.getLocal("info.rocketlauncher.tooltip").replaceAll("%s", String.valueOf(Settings.MAX_ROCKET_LAUCNHER_TIER));
    list.add(str);
  }
}
