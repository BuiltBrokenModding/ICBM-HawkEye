package icbm.explosion.explosive;

import calclavia.api.icbm.ICamouflageMaterial;
import calclavia.api.icbm.explosion.ExplosionEvent.ExplosivePreDetonationEvent;
import calclavia.api.icbm.explosion.ExplosiveType;
import calclavia.lib.utility.WrenchUtility;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.TabICBM;
import icbm.core.prefab.BlockICBM;
import icbm.explosion.entities.EntityExplosive;
import icbm.explosion.render.tile.RenderBombBlock;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.resources.Resource;
import net.minecraft.client.resources.ResourceManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AABBPool;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventBus;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;

public class BlockExplosive
  extends BlockICBM
  implements ICamouflageMaterial
{
  public final Icon[] ICON_TOP = new Icon[100];
  public final Icon[] ICON_SIDE = new Icon[100];
  public final Icon[] ICON_BOTTOM = new Icon[100];
  
  public BlockExplosive(int id)
  {
    super(id, "explosives", Material.field_76262_s);
    func_71848_c(0.0F);
    func_71884_a(field_71965_g);
    func_71849_a(TabICBM.INSTANCE);
  }
  
  private static byte determineOrientation(World world, int x, int y, int z, EntityLivingBase entityLiving)
  {
    if (entityLiving != null)
    {
      if ((MathHelper.func_76135_e((float)entityLiving.field_70165_t - x) < 2.0F) && (MathHelper.func_76135_e((float)entityLiving.field_70161_v - z) < 2.0F))
      {
        double var5 = entityLiving.field_70163_u + 1.82D - entityLiving.field_70129_M;
        if (var5 - y > 2.0D) {
          return 1;
        }
        if (y - var5 > 0.0D) {
          return 0;
        }
      }
      int rotation = MathHelper.func_76128_c(entityLiving.field_70177_z * 4.0F / 360.0F + 0.5D) & 0x3;
      return (byte)(rotation == 3 ? 4 : rotation == 2 ? 3 : rotation == 1 ? 5 : rotation == 0 ? 2 : 0);
    }
    return 0;
  }
  
  public void func_71902_a(IBlockAccess par1IBlockAccess, int x, int y, int z)
  {
    TileEntity tileEntity = par1IBlockAccess.func_72796_p(x, y, z);
    if (tileEntity != null) {
      if ((tileEntity instanceof TileExplosive)) {
        if (((TileExplosive)tileEntity).haoMa == Explosive.sMine.getID())
        {
          func_71905_a(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
          return;
        }
      }
    }
    func_71905_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
  }
  
  public void func_71919_f()
  {
    func_71905_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
  }
  
  public AxisAlignedBB func_71872_e(World par1World, int x, int y, int z)
  {
    TileEntity tileEntity = par1World.func_72796_p(x, y, z);
    if (tileEntity != null) {
      if ((tileEntity instanceof TileExplosive)) {
        if (((TileExplosive)tileEntity).haoMa == Explosive.sMine.getID()) {
          return AxisAlignedBB.func_72332_a().func_72299_a(x + this.field_72026_ch, y + this.field_72023_ci, z + this.field_72024_cj, x + this.field_72021_ck, y + 0.2D, z + this.field_72019_cm);
        }
      }
    }
    return super.func_71872_e(par1World, x, y, z);
  }
  
  public void func_71860_a(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack)
  {
    ((TileExplosive)world.func_72796_p(x, y, z)).haoMa = itemStack.func_77960_j();
    int explosiveID = ((TileExplosive)world.func_72796_p(x, y, z)).haoMa;
    if (!world.field_72995_K)
    {
      ExplosionEvent.ExplosivePreDetonationEvent evt = new ExplosionEvent.ExplosivePreDetonationEvent(world, x, y, z, ExplosiveType.BLOCK, ExplosiveRegistry.get(explosiveID));
      MinecraftForge.EVENT_BUS.post(evt);
      if (evt.isCanceled())
      {
        func_71897_c(world, x, y, z, explosiveID, 0);
        world.func_72832_d(x, y, z, 0, 0, 2);
        return;
      }
    }
    world.func_72921_c(x, y, z, VectorHelper.getOrientationFromSide(ForgeDirection.getOrientation(determineOrientation(world, x, y, z, entityLiving)), ForgeDirection.NORTH).ordinal(), 2);
    if (world.func_72864_z(x, y, z)) {
      yinZha(world, x, y, z, explosiveID, 0);
    }
    for (byte i = 0; i < 6; i = (byte)(i + 1))
    {
      Vector3 position = new Vector3(x, y, z);
      position.translate(ForgeDirection.getOrientation(i));
      
      int blockId = position.getBlockID(world);
      if ((blockId == Block.field_72067_ar.field_71990_ca) || (blockId == Block.field_71944_C.field_71990_ca) || (blockId == Block.field_71938_D.field_71990_ca)) {
        yinZha(world, x, y, z, explosiveID, 2);
      }
    }
    if (entityLiving != null) {
      FMLLog.fine(entityLiving.func_70023_ak() + " placed " + ExplosiveRegistry.get(explosiveID).getExplosiveName() + " in: " + x + ", " + y + ", " + z + ".", new Object[0]);
    }
  }
  
  public Icon func_71895_b(IBlockAccess par1IBlockAccess, int x, int y, int z, int side)
  {
    int explosiveID = ((TileExplosive)par1IBlockAccess.func_72796_p(x, y, z)).haoMa;
    return func_71858_a(side, explosiveID);
  }
  
  public Icon func_71858_a(int side, int explosiveID)
  {
    if (side == 0) {
      return this.ICON_BOTTOM[explosiveID];
    }
    if (side == 1) {
      return this.ICON_TOP[explosiveID];
    }
    return this.ICON_SIDE[explosiveID];
  }
  
  @SideOnly(Side.CLIENT)
  public void func_94332_a(IconRegister iconRegister)
  {
    for (Explosive zhaPin : )
    {
      this.ICON_TOP[zhaPin.getID()] = getIcon(iconRegister, zhaPin, "_top");
      this.ICON_SIDE[zhaPin.getID()] = getIcon(iconRegister, zhaPin, "_side");
      this.ICON_BOTTOM[zhaPin.getID()] = getIcon(iconRegister, zhaPin, "_bottom");
    }
  }
  
  @SideOnly(Side.CLIENT)
  public Icon getIcon(IconRegister iconRegister, Explosive zhaPin, String suffix)
  {
    String iconName = "explosive_" + zhaPin.getUnlocalizedName() + suffix;
    try
    {
      ResourceLocation resourcelocation = new ResourceLocation("icbm", "textures/blocks/" + iconName + ".png");
      InputStream inputstream = Minecraft.func_71410_x().func_110442_L().func_110536_a(resourcelocation).func_110527_b();
      BufferedImage bufferedimage = ImageIO.read(inputstream);
      if (bufferedimage != null) {
        return iconRegister.func_94245_a("icbm:" + iconName);
      }
    }
    catch (Exception e) {}
    if (suffix.equals("_bottom")) {
      return iconRegister.func_94245_a("icbm:explosive_bottom_" + zhaPin.getTier());
    }
    return iconRegister.func_94245_a("icbm:explosive_base_" + zhaPin.getTier());
  }
  
  public void func_71861_g(World par1World, int x, int y, int z)
  {
    super.func_71861_g(par1World, x, y, z);
    
    int explosiveID = ((TileExplosive)par1World.func_72796_p(x, y, z)).haoMa;
    par1World.func_72902_n(x, y, z);
  }
  
  public void func_71863_a(World world, int x, int y, int z, int blockId)
  {
    int explosiveID = ((TileExplosive)world.func_72796_p(x, y, z)).haoMa;
    if (world.func_72864_z(x, y, z)) {
      yinZha(world, x, y, z, explosiveID, 0);
    } else if ((blockId == Block.field_72067_ar.field_71990_ca) || (blockId == Block.field_71944_C.field_71990_ca) || (blockId == Block.field_71938_D.field_71990_ca)) {
      yinZha(world, x, y, z, explosiveID, 2);
    }
  }
  
  public static void yinZha(World world, int x, int y, int z, int explosiveID, int causeOfExplosion)
  {
    if (!world.field_72995_K)
    {
      TileEntity tileEntity = world.func_72796_p(x, y, z);
      if (tileEntity != null) {
        if ((tileEntity instanceof TileExplosive))
        {
          ExplosionEvent.ExplosivePreDetonationEvent evt = new ExplosionEvent.ExplosivePreDetonationEvent(world, x, y, z, ExplosiveType.BLOCK, ExplosiveRegistry.get(((TileExplosive)tileEntity).haoMa));
          MinecraftForge.EVENT_BUS.post(evt);
          if (!evt.isCanceled())
          {
            ((TileExplosive)tileEntity).exploding = true;
            EntityExplosive eZhaDan = new EntityExplosive(world, new Vector3(x, y, z).add(0.5D), ((TileExplosive)tileEntity).haoMa, (byte)world.func_72805_g(x, y, z), ((TileExplosive)tileEntity).nbtData);
            switch (causeOfExplosion)
            {
            case 2: 
              eZhaDan.func_70015_d(100);
            }
            world.func_72838_d(eZhaDan);
            world.func_94571_i(x, y, z);
          }
        }
      }
    }
  }
  
  public void onBlockExploded(World world, int x, int y, int z, Explosion explosion)
  {
    if (world.func_72796_p(x, y, z) != null)
    {
      int explosiveID = ((TileExplosive)world.func_72796_p(x, y, z)).haoMa;
      yinZha(world, x, y, z, explosiveID, 1);
    }
    super.onBlockExploded(world, x, y, z, explosion);
  }
  
  public boolean func_71903_a(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
  {
    TileEntity tileEntity = world.func_72796_p(x, y, z);
    if (entityPlayer.func_71045_bC() != null)
    {
      if (entityPlayer.func_71045_bC().field_77993_c == Item.field_77709_i.field_77779_bT)
      {
        int explosiveID = ((TileExplosive)tileEntity).haoMa;
        yinZha(world, x, y, z, explosiveID, 0);
        return true;
      }
      if (WrenchUtility.isUsableWrench(entityPlayer, entityPlayer.func_71045_bC(), x, y, z))
      {
        byte change = 3;
        switch (world.func_72805_g(x, y, z))
        {
        case 0: 
          change = 2;
          break;
        case 2: 
          change = 5;
          break;
        case 5: 
          change = 3;
          break;
        case 3: 
          change = 4;
          break;
        case 4: 
          change = 1;
          break;
        case 1: 
          change = 0;
        }
        world.func_72921_c(x, y, z, ForgeDirection.getOrientation(change).ordinal(), 3);
        
        world.func_72851_f(x, y, z, this.field_71990_ca);
        return true;
      }
    }
    if ((tileEntity instanceof TileExplosive)) {
      return ExplosiveRegistry.get(((TileExplosive)tileEntity).haoMa).onBlockActivated(world, x, y, z, entityPlayer, par6, par7, par8, par9);
    }
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public int func_71857_b()
  {
    return RenderBombBlock.ID;
  }
  
  public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
  {
    if (world.func_72796_p(x, y, z) != null)
    {
      int explosiveID = ((TileExplosive)world.func_72796_p(x, y, z)).haoMa;
      
      return new ItemStack(this.field_71990_ca, 1, explosiveID);
    }
    return null;
  }
  
  public void func_71852_a(World world, int x, int y, int z, int par5, int par6)
  {
    TileEntity tileEntity = world.func_72796_p(x, y, z);
    if (tileEntity != null) {
      if ((tileEntity instanceof TileExplosive)) {
        if (!((TileExplosive)tileEntity).exploding)
        {
          int explosiveID = ((TileExplosive)tileEntity).haoMa;
          int id = func_71885_a(world.func_72805_g(x, y, z), world.field_73012_v, 0);
          
          func_71929_a(world, x, y, z, new ItemStack(id, 1, explosiveID));
        }
      }
    }
    super.func_71852_a(world, x, y, z, par5, par6);
  }
  
  public int func_71925_a(Random par1Random)
  {
    return 0;
  }
  
  public void func_71879_a(int par1, CreativeTabs par2CreativeTabs, List par3List)
  {
    for (Explosive zhaPin : ) {
      if (zhaPin.hasBlockForm()) {
        par3List.add(new ItemStack(par1, 1, zhaPin.getID()));
      }
    }
  }
  
  public TileEntity func_72274_a(World var1)
  {
    return new TileExplosive();
  }
  
  public boolean func_71926_d()
  {
    return false;
  }
}
