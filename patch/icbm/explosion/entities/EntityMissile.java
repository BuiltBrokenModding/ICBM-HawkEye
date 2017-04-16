package icbm.explosion.entities;

import calclavia.api.icbm.ILauncherContainer;
import calclavia.api.icbm.IMissile;
import calclavia.api.icbm.ITarget;
import calclavia.api.icbm.ITarget.TargetType;
import calclavia.api.icbm.RadarRegistry;
import calclavia.api.icbm.explosion.ExplosionEvent.ExplosivePreDetonationEvent;
import calclavia.api.icbm.explosion.ExplosiveType;
import calclavia.api.icbm.explosion.IExplosive;
import calclavia.api.icbm.explosion.IExplosiveContainer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import icbm.Settings;
import icbm.core.DamageUtility;
import icbm.core.ICBMCore;
import icbm.core.implement.IChunkLoadHandler;
import icbm.explosion.CommonProxy;
import icbm.explosion.ICBMExplosion;
import icbm.explosion.ex.Ex;
import icbm.explosion.explosive.Explosive;
import icbm.explosion.explosive.ExplosiveRegistry;
import icbm.explosion.machines.TileCruiseLauncher;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventBus;
import universalelectricity.api.vector.Vector2;
import universalelectricity.api.vector.Vector3;

public class EntityMissile
  extends Entity
  implements IChunkLoadHandler, IExplosiveContainer, IEntityAdditionalSpawnData, IMissile, ITarget
{
  public static final float JIA_KUAI_SU_DU = 0.012F;
  
  public static enum MissileType
  {
    MISSILE,  CruiseMissile,  LAUNCHER;
    
    private MissileType() {}
  }
  
  public int explosiveID = 0;
  public int maxHeight = 200;
  public Vector3 targetVector = null;
  public Vector3 startPos = null;
  public Vector3 launcherPos = null;
  public boolean isExpoding = false;
  public int baoZhaGaoDu = 0;
  public int feiXingTick = -1;
  public double deltaPathX;
  public double deltaPathY;
  public double deltaPathZ;
  public double flatDistance;
  public float missileFlightTime;
  public float acceleration;
  public float damage = 0.0F;
  public float max_damage = 10.0F;
  public int protectionTime = 2;
  private ForgeChunkManager.Ticket chunkTicket;
  public Entity lockedTarget;
  public boolean didTargetLockBefore = false;
  public int trackingVar = -1;
  public int daoDanCount = 0;
  public double daoDanGaoDu = 2.0D;
  private boolean setExplode;
  private boolean setNormalExplode;
  public MissileType missileType = MissileType.MISSILE;
  public Vector3 xiaoDanMotion = new Vector3();
  private double qiFeiGaoDu = 3.0D;
  private final HashSet<Entity> ignoreEntity = new HashSet();
  protected final IUpdatePlayerListBox shengYin;
  public NBTTagCompound nbtData = new NBTTagCompound();
  
  public EntityMissile(World par1World)
  {
    super(par1World);
    func_70105_a(1.0F, 1.0F);
    this.field_70155_l = 3.0D;
    this.field_70178_ae = true;
    this.field_70158_ak = true;
    this.shengYin = (this.field_70170_p != null ? ICBMExplosion.proxy.getDaoDanShengYin(this) : null);
  }
  
  public EntityMissile(World world, Vector3 startPos, Vector3 launcherPos, int explosiveId)
  {
    this(world);
    
    this.startPos = startPos;
    this.launcherPos = launcherPos;
    
    func_70107_b(this.startPos.x, this.startPos.y, this.startPos.z);
    func_70101_b(0.0F, 90.0F);
  }
  
  public EntityMissile(World world, Vector3 startPos, int explosiveId, float yaw, float pitch)
  {
    this(world);
    
    this.launcherPos = (this.startPos = startPos);
    this.missileType = MissileType.LAUNCHER;
    this.protectionTime = 0;
    
    func_70107_b(this.startPos.x, this.startPos.y, this.startPos.z);
    func_70101_b(yaw, pitch);
  }
  
  public String func_70023_ak()
  {
    return ExplosiveRegistry.get(this.explosiveID).getMissileName();
  }
  
  public void writeSpawnData(ByteArrayDataOutput data)
  {
    try
    {
      data.writeInt(this.explosiveID);
      data.writeInt(this.missileType.ordinal());
      
      data.writeDouble(this.startPos.x);
      data.writeDouble(this.startPos.y);
      data.writeDouble(this.startPos.z);
      
      data.writeInt(this.launcherPos.intX());
      data.writeInt(this.launcherPos.intY());
      data.writeInt(this.launcherPos.intZ());
      
      data.writeFloat(this.field_70177_z);
      data.writeFloat(this.field_70125_A);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void readSpawnData(ByteArrayDataInput data)
  {
    try
    {
      this.explosiveID = data.readInt();
      this.missileType = MissileType.values()[data.readInt()];
      this.startPos = new Vector3(data.readDouble(), data.readDouble(), data.readDouble());
      this.launcherPos = new Vector3(data.readInt(), data.readInt(), data.readInt());
      
      this.field_70177_z = data.readFloat();
      this.field_70125_A = data.readFloat();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void launch(Vector3 target)
  {
    this.startPos = new Vector3(this);
    this.targetVector = target;
    this.baoZhaGaoDu = this.targetVector.intY();
    ((Ex)ExplosiveRegistry.get(this.explosiveID)).launch(this);
    this.feiXingTick = 0;
    recalculatePath();
    this.field_70170_p.func_72956_a(this, "icbm:missilelaunch", 4.0F, (1.0F + (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) * 0.2F) * 0.7F);
    
    RadarRegistry.register(this);
    ICBMCore.LOGGER.info("Launching " + func_70023_ak() + " (" + this.field_70157_k + ") from " + this.startPos.intX() + ", " + this.startPos.intY() + ", " + this.startPos.intZ() + " to " + this.targetVector.intX() + ", " + this.targetVector.intY() + ", " + this.targetVector.intZ());
  }
  
  public void launch(Vector3 target, int height)
  {
    this.qiFeiGaoDu = height;
    launch(target);
  }
  
  public EntityMissile ignore(Entity entity)
  {
    this.ignoreEntity.add(entity);
    return this;
  }
  
  public void recalculatePath()
  {
    if (this.targetVector != null)
    {
      this.deltaPathX = (this.targetVector.x - this.startPos.x);
      this.deltaPathY = (this.targetVector.y - this.startPos.y);
      this.deltaPathZ = (this.targetVector.z - this.startPos.z);
      
      this.flatDistance = Vector2.distance(this.startPos.toVector2(), this.targetVector.toVector2());
      
      this.maxHeight = (160 + (int)(this.flatDistance * 3.0D));
      
      this.missileFlightTime = ((float)Math.max(100.0D, 2.0D * this.flatDistance) - this.feiXingTick);
      
      this.acceleration = (this.maxHeight * 2.0F / (this.missileFlightTime * this.missileFlightTime));
    }
  }
  
  public void func_70088_a()
  {
    this.field_70180_af.func_75682_a(16, Integer.valueOf(-1));
    this.field_70180_af.func_75682_a(17, Integer.valueOf(0));
    chunkLoaderInit(ForgeChunkManager.requestTicket(ICBMExplosion.instance, this.field_70170_p, ForgeChunkManager.Type.ENTITY));
  }
  
  public void chunkLoaderInit(ForgeChunkManager.Ticket ticket)
  {
    if (!this.field_70170_p.field_72995_K) {
      if (ticket != null)
      {
        if (this.chunkTicket == null)
        {
          this.chunkTicket = ticket;
          this.chunkTicket.bindEntity(this);
          this.chunkTicket.getModData();
        }
        ForgeChunkManager.forceChunk(this.chunkTicket, new ChunkCoordIntPair(this.field_70176_ah, this.field_70164_aj));
      }
    }
  }
  
  final List<ChunkCoordIntPair> loadedChunks = new ArrayList();
  
  public void updateLoadChunk(int newChunkX, int newChunkZ)
  {
    if ((!this.field_70170_p.field_72995_K) && (Settings.LOAD_CHUNKS) && (this.chunkTicket != null))
    {
      for (ChunkCoordIntPair chunk : this.loadedChunks) {
        ForgeChunkManager.unforceChunk(this.chunkTicket, chunk);
      }
      this.loadedChunks.clear();
      this.loadedChunks.add(new ChunkCoordIntPair(newChunkX, newChunkZ));
      this.loadedChunks.add(new ChunkCoordIntPair(newChunkX + 1, newChunkZ + 1));
      this.loadedChunks.add(new ChunkCoordIntPair(newChunkX - 1, newChunkZ - 1));
      this.loadedChunks.add(new ChunkCoordIntPair(newChunkX + 1, newChunkZ - 1));
      this.loadedChunks.add(new ChunkCoordIntPair(newChunkX - 1, newChunkZ + 1));
      for (ChunkCoordIntPair chunk : this.loadedChunks) {
        ForgeChunkManager.forceChunk(this.chunkTicket, chunk);
      }
    }
  }
  
  public boolean func_70067_L()
  {
    return true;
  }
  
  public void func_70071_h_()
  {
    if (this.shengYin != null) {
      this.shengYin.func_73660_a();
    }
    if (!this.field_70170_p.field_72995_K)
    {
      ExplosionEvent.ExplosivePreDetonationEvent evt = new ExplosionEvent.ExplosivePreDetonationEvent(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, ExplosiveType.AIR, ExplosiveRegistry.get(this.explosiveID));
      MinecraftForge.EVENT_BUS.post(evt);
      if (evt.isCanceled())
      {
        if (this.feiXingTick >= 0) {
          dropMissileAsItem();
        }
        func_70106_y();
        return;
      }
    }
    try
    {
      if (this.field_70170_p.field_72995_K)
      {
        this.feiXingTick = this.field_70180_af.func_75679_c(16);
        int status = this.field_70180_af.func_75679_c(17);
        switch (status)
        {
        case 1: 
          this.setNormalExplode = true;
          break;
        case 2: 
          this.setExplode = true;
        }
      }
      else
      {
        this.field_70180_af.func_75692_b(16, Integer.valueOf(this.feiXingTick));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    if (this.setNormalExplode)
    {
      normalExplode();
      return;
    }
    if (this.setExplode)
    {
      explode();
      return;
    }
    if (this.feiXingTick >= 0)
    {
      RadarRegistry.register(this);
      if (!this.field_70170_p.field_72995_K)
      {
        if ((this.missileType == MissileType.CruiseMissile) || (this.missileType == MissileType.LAUNCHER))
        {
          if ((this.feiXingTick == 0) && (this.xiaoDanMotion != null))
          {
            this.xiaoDanMotion = new Vector3(this.deltaPathX / (this.missileFlightTime * 0.3D), this.deltaPathY / (this.missileFlightTime * 0.3D), this.deltaPathZ / (this.missileFlightTime * 0.3D));
            this.field_70159_w = this.xiaoDanMotion.x;
            this.field_70181_x = this.xiaoDanMotion.y;
            this.field_70179_y = this.xiaoDanMotion.z;
          }
          this.field_70125_A = ((float)(Math.atan(this.field_70181_x / Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y)) * 180.0D / 3.141592653589793D));
          
          this.field_70177_z = ((float)(Math.atan2(this.field_70159_w, this.field_70179_y) * 180.0D / 3.141592653589793D));
          
          ((Ex)ExplosiveRegistry.get(this.explosiveID)).update(this);
          
          Block block = Block.field_71973_m[this.field_70170_p.func_72798_a((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v)];
          if ((this.protectionTime <= 0) && (((block != null) && (!(block instanceof BlockFluid))) || (this.field_70163_u > 1000.0D) || (this.field_70132_H) || (this.feiXingTick > 20000) || ((this.field_70159_w == 0.0D) && (this.field_70181_x == 0.0D) && (this.field_70179_y == 0.0D))))
          {
            setExplode();
            return;
          }
          func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
        }
        else if (this.qiFeiGaoDu > 0.0D)
        {
          this.field_70181_x = (0.012F * this.feiXingTick * (this.feiXingTick / 2));
          this.field_70159_w = 0.0D;
          this.field_70179_y = 0.0D;
          this.qiFeiGaoDu -= this.field_70181_x;
          func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
          if (this.qiFeiGaoDu <= 0.0D)
          {
            this.field_70181_x = (this.acceleration * (this.missileFlightTime / 2.0F));
            this.field_70159_w = (this.deltaPathX / this.missileFlightTime);
            this.field_70179_y = (this.deltaPathZ / this.missileFlightTime);
          }
        }
        else
        {
          this.field_70181_x -= this.acceleration;
          
          this.field_70125_A = ((float)(Math.atan(this.field_70181_x / Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y)) * 180.0D / 3.141592653589793D));
          
          this.field_70177_z = ((float)(Math.atan2(this.field_70159_w, this.field_70179_y) * 180.0D / 3.141592653589793D));
          
          ((Ex)ExplosiveRegistry.get(this.explosiveID)).update(this);
          
          func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
          if (this.field_70132_H) {
            explode();
          }
          if ((this.baoZhaGaoDu > 0) && (this.field_70181_x < 0.0D))
          {
            int blockBelowID = this.field_70170_p.func_72798_a((int)this.field_70165_t, (int)this.field_70163_u - this.baoZhaGaoDu, (int)this.field_70161_v);
            if (blockBelowID > 0)
            {
              this.baoZhaGaoDu = 0;
              explode();
            }
          }
        }
      }
      else
      {
        this.field_70125_A = ((float)(Math.atan(this.field_70181_x / Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y)) * 180.0D / 3.141592653589793D));
        
        this.field_70177_z = ((float)(Math.atan2(this.field_70159_w, this.field_70179_y) * 180.0D / 3.141592653589793D));
      }
      this.field_70142_S = this.field_70165_t;
      this.field_70137_T = this.field_70163_u;
      this.field_70136_U = this.field_70161_v;
      
      spawnMissileSmoke();
      this.protectionTime -= 1;
      this.feiXingTick += 1;
    }
    else if (this.missileType != MissileType.LAUNCHER)
    {
      ILauncherContainer launcher = getLauncher();
      if (launcher != null)
      {
        launcher.setContainingMissile(this);
        if ((launcher instanceof TileCruiseLauncher))
        {
          this.missileType = MissileType.CruiseMissile;
          this.field_70145_X = true;
          if (this.field_70170_p.field_72995_K)
          {
            this.field_70177_z = (-((TileCruiseLauncher)launcher).rotationYaw + 90.0F);
            this.field_70125_A = ((TileCruiseLauncher)launcher).rotationPitch;
          }
          this.field_70163_u = (((TileCruiseLauncher)launcher).field_70330_m + 1);
        }
      }
      else
      {
        func_70106_y();
      }
    }
    super.func_70071_h_();
  }
  
  public ILauncherContainer getLauncher()
  {
    if (this.launcherPos != null)
    {
      TileEntity tileEntity = this.launcherPos.getTileEntity(this.field_70170_p);
      if ((tileEntity != null) && ((tileEntity instanceof ILauncherContainer))) {
        if (!tileEntity.func_70320_p()) {
          return (ILauncherContainer)tileEntity;
        }
      }
    }
    return null;
  }
  
  public boolean func_130002_c(EntityPlayer entityPlayer)
  {
    if ((Ex)ExplosiveRegistry.get(this.explosiveID) != null) {
      if (((Ex)ExplosiveRegistry.get(this.explosiveID)).onInteract(this, entityPlayer)) {
        return true;
      }
    }
    if ((!this.field_70170_p.field_72995_K) && ((this.field_70153_n == null) || (this.field_70153_n == entityPlayer)))
    {
      entityPlayer.func_70078_a(this);
      return true;
    }
    return false;
  }
  
  public double func_70042_X()
  {
    if ((this.missileFlightTime <= 0.0F) && (this.missileType == MissileType.MISSILE)) {
      return this.field_70131_O;
    }
    if (this.missileType == MissileType.CruiseMissile) {
      return this.field_70131_O / 10.0F;
    }
    return this.field_70131_O / 2.0F + this.field_70181_x;
  }
  
  private void spawnMissileSmoke()
  {
    if (this.field_70170_p.field_72995_K)
    {
      Vector3 position = new Vector3(this);
      
      double distance = -this.daoDanGaoDu - 0.20000000298023224D;
      Vector3 delta = new Vector3();
      
      delta.y = (Math.sin(Math.toRadians(this.field_70125_A)) * distance);
      
      double dH = Math.cos(Math.toRadians(this.field_70125_A)) * distance;
      
      delta.x = (Math.sin(Math.toRadians(this.field_70177_z)) * dH);
      delta.z = (Math.cos(Math.toRadians(this.field_70177_z)) * dH);
      
      position.add(delta);
      this.field_70170_p.func_72869_a("flame", position.x, position.y, position.z, 0.0D, 0.0D, 0.0D);
      ICBMExplosion.proxy.spawnParticle("missile_smoke", this.field_70170_p, position, 4.0F, 2.0D);
      position.scale(1.0D - 0.001D * Math.random());
      ICBMExplosion.proxy.spawnParticle("missile_smoke", this.field_70170_p, position, 4.0F, 2.0D);
      position.scale(1.0D - 0.001D * Math.random());
      ICBMExplosion.proxy.spawnParticle("missile_smoke", this.field_70170_p, position, 4.0F, 2.0D);
      position.scale(1.0D - 0.001D * Math.random());
      ICBMExplosion.proxy.spawnParticle("missile_smoke", this.field_70170_p, position, 4.0F, 2.0D);
    }
  }
  
  public AxisAlignedBB func_70114_g(Entity entity)
  {
    if (this.ignoreEntity.contains(entity)) {
      return null;
    }
    if ((!(entity instanceof EntityItem)) && (entity != this.field_70153_n) && (this.protectionTime <= 0))
    {
      if ((entity instanceof EntityMissile)) {
        ((EntityMissile)entity).setNormalExplode();
      }
      setExplode();
    }
    return null;
  }
  
  public Vector3 getPredictedPosition(int t)
  {
    Vector3 guJiDiDian = new Vector3(this);
    double tempMotionY = this.field_70181_x;
    if (this.feiXingTick > 20) {
      for (int i = 0; i < t; i++) {
        if ((this.missileType == MissileType.CruiseMissile) || (this.missileType == MissileType.LAUNCHER))
        {
          guJiDiDian.x += this.xiaoDanMotion.x;
          guJiDiDian.y += this.xiaoDanMotion.y;
          guJiDiDian.z += this.xiaoDanMotion.z;
        }
        else
        {
          guJiDiDian.x += this.field_70159_w;
          guJiDiDian.y += tempMotionY;
          guJiDiDian.z += this.field_70179_y;
          
          tempMotionY -= this.acceleration;
        }
      }
    }
    return guJiDiDian;
  }
  
  public void setNormalExplode()
  {
    this.setNormalExplode = true;
    this.field_70180_af.func_75692_b(17, Integer.valueOf(1));
  }
  
  public void setExplode()
  {
    this.setExplode = true;
    this.field_70180_af.func_75692_b(17, Integer.valueOf(2));
  }
  
  public void func_70106_y()
  {
    RadarRegistry.unregister(this);
    if (this.chunkTicket != null) {
      ForgeChunkManager.releaseTicket(this.chunkTicket);
    }
    super.func_70106_y();
    if (this.shengYin != null) {
      this.shengYin.func_73660_a();
    }
  }
  
  public void explode()
  {
    try
    {
      if (!this.isExpoding)
      {
        if (this.explosiveID == 0)
        {
          if (!this.field_70170_p.field_72995_K) {
            this.field_70170_p.func_72876_a(this, this.field_70165_t, this.field_70163_u, this.field_70161_v, 5.0F, true);
          }
        }
        else {
          ((Ex)ExplosiveRegistry.get(this.explosiveID)).createExplosion(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, this);
        }
        this.isExpoding = true;
        
        ICBMCore.LOGGER.info(func_70023_ak() + " (" + this.field_70157_k + ") exploded in " + (int)this.field_70165_t + ", " + (int)this.field_70163_u + ", " + (int)this.field_70161_v);
      }
      func_70106_y();
    }
    catch (Exception e)
    {
      ICBMCore.LOGGER.severe("Missile failed to explode properly. Report this to the developers.");
      e.printStackTrace();
    }
  }
  
  public void normalExplode()
  {
    if (!this.isExpoding)
    {
      this.isExpoding = true;
      if (!this.field_70170_p.field_72995_K) {
        this.field_70170_p.func_72876_a(this, this.field_70165_t, this.field_70163_u, this.field_70161_v, 5.0F, true);
      }
      func_70106_y();
    }
  }
  
  public void dropMissileAsItem()
  {
    if ((!this.isExpoding) && (!this.field_70170_p.field_72995_K))
    {
      EntityItem entityItem = new EntityItem(this.field_70170_p, this.field_70165_t, this.field_70163_u, this.field_70161_v, new ItemStack(ICBMExplosion.itemMissile, 1, this.explosiveID));
      
      float var13 = 0.05F;
      Random random = new Random();
      entityItem.field_70159_w = ((float)random.nextGaussian() * var13);
      entityItem.field_70181_x = ((float)random.nextGaussian() * var13 + 0.2F);
      entityItem.field_70179_y = ((float)random.nextGaussian() * var13);
      this.field_70170_p.func_72838_d(entityItem);
    }
    func_70106_y();
  }
  
  protected void func_70037_a(NBTTagCompound nbt)
  {
    this.startPos = new Vector3(nbt.func_74775_l("kaiShi"));
    this.targetVector = new Vector3(nbt.func_74775_l("muBiao"));
    this.launcherPos = new Vector3(nbt.func_74775_l("faSheQi"));
    this.acceleration = nbt.func_74760_g("jiaSu");
    this.baoZhaGaoDu = nbt.func_74762_e("baoZhaGaoDu");
    this.explosiveID = nbt.func_74762_e("haoMa");
    this.feiXingTick = nbt.func_74762_e("feiXingTick");
    this.qiFeiGaoDu = nbt.func_74769_h("qiFeiGaoDu");
    this.missileType = MissileType.values()[nbt.func_74762_e("xingShi")];
    this.nbtData = nbt.func_74775_l("data");
  }
  
  protected void func_70014_b(NBTTagCompound nbt)
  {
    if (this.startPos != null) {
      nbt.func_74766_a("kaiShi", this.startPos.writeToNBT(new NBTTagCompound()));
    }
    if (this.targetVector != null) {
      nbt.func_74766_a("muBiao", this.targetVector.writeToNBT(new NBTTagCompound()));
    }
    if (this.launcherPos != null) {
      nbt.func_74766_a("faSheQi", this.launcherPos.writeToNBT(new NBTTagCompound()));
    }
    nbt.func_74776_a("jiaSu", this.acceleration);
    nbt.func_74768_a("haoMa", this.explosiveID);
    nbt.func_74768_a("baoZhaGaoDu", this.baoZhaGaoDu);
    nbt.func_74768_a("feiXingTick", this.feiXingTick);
    nbt.func_74780_a("qiFeiGaoDu", this.qiFeiGaoDu);
    nbt.func_74768_a("xingShi", this.missileType.ordinal());
    nbt.func_74782_a("data", this.nbtData);
  }
  
  public float func_70053_R()
  {
    return 1.0F;
  }
  
  public int getTicksInAir()
  {
    return this.feiXingTick;
  }
  
  public IExplosive getExplosiveType()
  {
    return ExplosiveRegistry.get(this.explosiveID);
  }
  
  public boolean func_70097_a(DamageSource source, float damage)
  {
    if (DamageUtility.canHarm(this, source, damage))
    {
      this.damage += damage;
      if (this.damage >= this.max_damage) {
        func_70106_y();
      }
      return true;
    }
    return false;
  }
  
  public boolean canBeTargeted(Object turret)
  {
    return getTicksInAir() > 0;
  }
  
  public ITarget.TargetType getType()
  {
    return ITarget.TargetType.MISSILE;
  }
  
  public NBTTagCompound getTagCompound()
  {
    return this.nbtData;
  }
}
