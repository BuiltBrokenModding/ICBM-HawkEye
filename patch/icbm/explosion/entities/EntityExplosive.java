package icbm.explosion.entities;

import calclavia.api.icbm.explosion.ExplosionEvent.ExplosivePreDetonationEvent;
import calclavia.api.icbm.explosion.ExplosiveType;
import calclavia.api.icbm.explosion.IExplosive;
import calclavia.api.icbm.explosion.IExplosiveContainer;
import calclavia.lib.prefab.tile.IRotatable;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import icbm.explosion.ICBMExplosion;
import icbm.explosion.explosive.Explosive;
import icbm.explosion.explosive.ExplosiveRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventBus;
import universalelectricity.api.vector.Vector3;

public class EntityExplosive
  extends Entity
  implements IRotatable, IEntityAdditionalSpawnData, IExplosiveContainer
{
  public int fuse = 90;
  public int explosiveID = 0;
  private byte orientation = 3;
  public NBTTagCompound nbtData = new NBTTagCompound();
  
  public EntityExplosive(World par1World)
  {
    super(par1World);
    this.fuse = 0;
    this.field_70156_m = true;
    func_70105_a(0.98F, 0.98F);
    this.field_70129_M = (this.field_70131_O / 2.0F);
  }
  
  public EntityExplosive(World par1World, Vector3 position, byte orientation, int explosiveID)
  {
    this(par1World);
    func_70107_b(position.x, position.y, position.z);
    float var8 = (float)(Math.random() * 3.141592653589793D * 2.0D);
    this.field_70159_w = (-(float)Math.sin(var8) * 0.02F);
    this.field_70181_x = 0.20000000298023224D;
    this.field_70179_y = (-(float)Math.cos(var8) * 0.02F);
    this.field_70169_q = position.x;
    this.field_70167_r = position.y;
    this.field_70166_s = position.z;
    this.explosiveID = explosiveID;
    
    this.orientation = orientation;
    
    ExplosiveRegistry.get(explosiveID).yinZhaQian(par1World, this);
  }
  
  public EntityExplosive(World par1World, Vector3 position, int explosiveID, byte orientation, NBTTagCompound nbtData)
  {
    this(par1World, position, orientation, explosiveID);
    this.nbtData = nbtData;
  }
  
  public String func_70023_ak()
  {
    return "Explosives";
  }
  
  public void func_70071_h_()
  {
    if (!this.field_70170_p.field_72995_K)
    {
      ExplosionEvent.ExplosivePreDetonationEvent evt = new ExplosionEvent.ExplosivePreDetonationEvent(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, ExplosiveType.BLOCK, ExplosiveRegistry.get(this.explosiveID));
      MinecraftForge.EVENT_BUS.post(evt);
      if (evt.isCanceled())
      {
        ICBMExplosion.blockExplosive.func_71897_c(this.field_70170_p, (int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v, this.explosiveID, 0);
        func_70106_y();
        return;
      }
    }
    this.field_70169_q = this.field_70165_t;
    this.field_70167_r = this.field_70163_u;
    this.field_70166_s = this.field_70161_v;
    
    this.field_70159_w *= 0.95D;
    this.field_70181_x -= 0.045D;
    this.field_70179_y *= 0.95D;
    
    func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
    if (this.fuse < 1) {
      explode();
    } else {
      ExplosiveRegistry.get(this.explosiveID).onYinZha(this.field_70170_p, new Vector3(this.field_70165_t, this.field_70163_u, this.field_70161_v), this.fuse);
    }
    this.fuse -= 1;
    
    super.func_70071_h_();
  }
  
  public void explode()
  {
    this.field_70170_p.func_72869_a("hugeexplosion", this.field_70165_t, this.field_70163_u, this.field_70161_v, 0.0D, 0.0D, 0.0D);
    getExplosiveType().createExplosion(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, this);
    func_70106_y();
  }
  
  protected void func_70037_a(NBTTagCompound nbt)
  {
    this.fuse = nbt.func_74771_c("Fuse");
    this.explosiveID = nbt.func_74762_e("explosiveID");
    this.nbtData = nbt.func_74775_l("data");
  }
  
  protected void func_70014_b(NBTTagCompound nbt)
  {
    nbt.func_74774_a("Fuse", (byte)this.fuse);
    nbt.func_74768_a("explosiveID", this.explosiveID);
    nbt.func_74782_a("data", this.nbtData);
  }
  
  public float func_70053_R()
  {
    return 0.5F;
  }
  
  protected void func_70088_a() {}
  
  protected boolean func_70041_e_()
  {
    return true;
  }
  
  public boolean func_70067_L()
  {
    return true;
  }
  
  public boolean func_70104_M()
  {
    return true;
  }
  
  public ForgeDirection getDirection()
  {
    return ForgeDirection.getOrientation(this.orientation);
  }
  
  public void setDirection(ForgeDirection facingDirection)
  {
    this.orientation = ((byte)facingDirection.ordinal());
  }
  
  public void writeSpawnData(ByteArrayDataOutput data)
  {
    data.writeInt(this.explosiveID);
    data.writeInt(this.fuse);
    data.writeByte(this.orientation);
  }
  
  public void readSpawnData(ByteArrayDataInput data)
  {
    this.explosiveID = data.readInt();
    this.fuse = data.readInt();
    this.orientation = data.readByte();
  }
  
  public IExplosive getExplosiveType()
  {
    return ExplosiveRegistry.get(this.explosiveID);
  }
  
  public NBTTagCompound getTagCompound()
  {
    return this.nbtData;
  }
}
