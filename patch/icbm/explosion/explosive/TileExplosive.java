package icbm.explosion.explosive;

import calclavia.api.icbm.explosion.IExplosive;
import calclavia.api.icbm.explosion.IExplosiveContainer;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketTile;
import calclavia.lib.prefab.tile.IRotatable;
import com.google.common.io.ByteArrayDataInput;
import icbm.core.ICBMCore;
import icbm.explosion.ICBMExplosion;
import icbm.explosion.items.ItemRemoteDetonator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class TileExplosive
  extends TileEntity
  implements IExplosiveContainer, IPacketReceiver, IRotatable
{
  public boolean exploding = false;
  public int haoMa = 0;
  public NBTTagCompound nbtData = new NBTTagCompound();
  
  public boolean canUpdate()
  {
    return false;
  }
  
  public void func_70307_a(NBTTagCompound par1NBTTagCompound)
  {
    super.func_70307_a(par1NBTTagCompound);
    this.haoMa = par1NBTTagCompound.func_74762_e("explosiveID");
    this.nbtData = par1NBTTagCompound.func_74775_l("data");
  }
  
  public void func_70310_b(NBTTagCompound par1NBTTagCompound)
  {
    super.func_70310_b(par1NBTTagCompound);
    par1NBTTagCompound.func_74768_a("explosiveID", this.haoMa);
    par1NBTTagCompound.func_74782_a("data", this.nbtData);
  }
  
  public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
  {
    try
    {
      byte ID = data.readByte();
      if (ID == 1)
      {
        this.haoMa = data.readInt();
        this.field_70331_k.func_72902_n(this.field_70329_l, this.field_70330_m, this.field_70327_n);
      }
      else if ((ID == 2) && (!this.field_70331_k.field_72995_K))
      {
        if ((player.field_71071_by.func_70448_g().func_77973_b() instanceof ItemRemoteDetonator))
        {
          ItemStack itemStack = player.field_71071_by.func_70448_g();
          BlockExplosive.yinZha(this.field_70331_k, this.field_70329_l, this.field_70330_m, this.field_70327_n, this.haoMa, 0);
          ((ItemRemoteDetonator)ICBMExplosion.itemRemoteDetonator).discharge(itemStack, 1500L, true);
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public Packet func_70319_e()
  {
    return ICBMCore.PACKET_TILE.getPacket(this, new Object[] { Byte.valueOf((byte) 1), Integer.valueOf(this.haoMa) });
  }
  
  public ForgeDirection getDirection()
  {
    return ForgeDirection.getOrientation(func_70322_n());
  }
  
  public void setDirection(ForgeDirection facingDirection)
  {
    this.field_70331_k.func_72921_c(this.field_70329_l, this.field_70330_m, this.field_70327_n, facingDirection.ordinal(), 2);
  }
  
  public IExplosive getExplosiveType()
  {
    return ExplosiveRegistry.get(this.haoMa);
  }
  
  public NBTTagCompound getTagCompound()
  {
    return this.nbtData;
  }
}
