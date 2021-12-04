package wtf.moneymod.client.impl.module.player;

import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.material.Material;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import wtf.moneymod.client.Main;
import wtf.moneymod.client.api.events.MotionUpdateEvent;
import wtf.moneymod.client.api.events.PreUpdateEvent;
import wtf.moneymod.client.api.management.impl.FriendManagement;
import wtf.moneymod.client.api.setting.annotatable.Value;
import wtf.moneymod.client.impl.module.Module;
import wtf.moneymod.client.impl.utility.impl.player.ItemUtil;
import wtf.moneymod.client.mixin.mixins.ducks.IMinecraft;
import wtf.moneymod.eventhandler.listener.Handler;
import wtf.moneymod.eventhandler.listener.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Module.Register( label = "AutoMine", desc = "Automatically mines", cat = Module.Category.PLAYER )
public class AutoMine extends Module
{
    @Value( "Mode" ) public Mode mode = Mode.FEET;
    @Value( "Focused" ) public boolean focused = true;
    @Value( "RequirePickaxe" ) public boolean requirepickaxe = false;
    @Value( "Switch" ) public boolean switchbool = false;
    @Value( "AutoDisable" ) public boolean autodisable = false;

    public enum Mode
    {
        FEET, CONTINIOUS
    }

    private BlockPos blockpos = null;

    @Override
    public void onEnable( )
    {
        blockpos = null;
    }

    @Override
    public void onDisable( )
    {
        blockpos = null;
        KeyBinding.setKeyBindState( mc.gameSettings.keyBindAttack.getKeyCode( ), false );
    }

    @Handler
    public Listener< PreUpdateEvent > onPreUpdate = new Listener< >( PreUpdateEvent.class, event ->
    {
        if( mode == Mode.CONTINIOUS )
        {
            if( !focused )
                ( ( IMinecraft )mc ).mm_setLeftClickCounter( 0 );

            ( ( IMinecraft )mc ).mm_invokeSendClickBlockToController( true );
        }
    } );

    @Handler
    public Listener< MotionUpdateEvent > onMotionUpdate = new Listener< >( MotionUpdateEvent.class, event ->
    {
        if( event.stage != 0 ) return;

        if( mode == Mode.CONTINIOUS ) return;

        if( !switchbool || checkPickaxe( ) )
        {
            if( blockpos != null )
            {
                if( mc.world.getBlockState( blockpos ).getBlock( ).equals( Blocks.AIR ) )
                {
                    if( autodisable )
                    {
                        disable( );
                        return;
                    }

                    blockpos = null;
                }
            }

            BlockPos blockpos2 = null;
            for( Entity obj : mc.world.playerEntities.stream( ).filter( player ->
            {
                return player != mc.player && !FriendManagement.getInstance( ).is( player.getName( ) ) && Float.compare( mc.player.getDistance( player ), 7.0f ) < 0;
            } ).collect( Collectors.toList( ) ) )
            {
                BlockPos pos = new BlockPos( obj.getPositionVector( ) );
                if( !checkBlockPos( pos ) ) continue;

                for( BlockPos pos2 : blockPosList( pos ) )
                {
                    if( !( mc.world.getBlockState( pos2 ).getBlock( ) instanceof BlockObsidian ) ) continue;
                    if( !mc.world.getBlockState( pos2.add( 0, 1, 0 ) ).getMaterial( ).equals( Material.AIR ) ) continue;

                    double dist = mc.player.getDistance( pos2.getX( ), pos2.getY( ), pos2.getZ( ) );
                    if( dist < 5.0 )
                    {
                        blockpos2 = pos2;
                        break;
                    }
                }
            }

            if( blockpos2 != null )
            {
                if( switchbool && ItemUtil.findItem( Items.DIAMOND_PICKAXE.getClass( ) ) != -1 )
                    mc.player.inventory.currentItem = ItemUtil.findItem( Items.DIAMOND_PICKAXE.getClass( ) );

                float[ ] rotation = shitMethod2( blockpos2 );
                event.rotationYaw = rotation[ 0 ];
                event.rotationPitch = rotation[ 1 ];

                if( !requirepickaxe || mc.player.getHeldItemMainhand( ).getItem( ).equals( Items.DIAMOND_PICKAXE ) )
                {
                    if( blockpos != null )
                    {
                        if( blockpos.equals( blockpos2 ) )
                        {
                            if( Main.getMain( ).getModuleManager( ).get( SpeedMine.class ).isToggled( ) )
                                return;
                        }
                    }

                    mc.playerController.onPlayerDamageBlock( blockpos2, getFacing( blockpos2 ) );
                    mc.player.swingArm( EnumHand.MAIN_HAND );
                    this.blockpos = blockpos2;
                }
            }
        }
    } );

    public boolean checkPickaxe( )
    {
        Item item = mc.player.getHeldItemMainhand( ).getItem( );

        return item.equals( Items.DIAMOND_PICKAXE ) || item.equals( Items.IRON_PICKAXE ) ||
                item.equals( Items.GOLDEN_PICKAXE ) || item.equals( Items.STONE_PICKAXE ) ||
                item.equals( Items.WOODEN_PICKAXE );
    }

    // AUTISM BELOW
    //rhd
    public boolean checkValidBlock( Block block )
    {
        return block.equals( Blocks.OBSIDIAN ) || block.equals( Blocks.BEDROCK );
    }

    //rhn
    public boolean checkBlockPos( BlockPos blockPos ) {
        Block block = mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock();
        Block block2 = mc.world.getBlockState(blockPos.add(0, 0, -1)).getBlock();
        Block block3 = mc.world.getBlockState(blockPos.add(1, 0, 0)).getBlock();
        Block block4 = mc.world.getBlockState(blockPos.add(0, 0, 1)).getBlock();
        Block block5 = mc.world.getBlockState(blockPos.add(-1, 0, 0)).getBlock();
        if (mc.world.isAirBlock(blockPos)) {
            if (mc.world.isAirBlock(blockPos.add(0, 1, 0))) {
                if (mc.world.isAirBlock(blockPos.add(0, 2, 0))) {
                    if (checkValidBlock(block)) {
                        if (checkValidBlock(block2)) {
                            if (checkValidBlock(block3)) {
                                if (checkValidBlock(block4)) {
                                    if (checkValidBlock(block5)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    //rhf
    public static List<BlockPos> blockPosList(BlockPos blockPos) {
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>();
        arrayList.add(blockPos.add(1, 0, 0));
        arrayList.add(blockPos.add(-1, 0, 0));
        arrayList.add(blockPos.add(0, 0, 1));
        arrayList.add(blockPos.add(0, 0, -1));
        return arrayList;
    }

    //rhC
    public static Vec3d vec3dPosition() {
        return new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ);
    }

    //rhX
    public static float[] shitMethod(Vec3d vec3d) {
        Vec3d vec3d2 = vec3dPosition();
        Vec3d vec3d3 = vec3d;
        double d = vec3d3.x - vec3d2.x;
        double d2 = vec3d3.y - vec3d2.y;
        double d3 = vec3d3.z - vec3d2.z;
        double d4 = d;
        double d5 = d3;
        double d6 = Math.sqrt(d4 * d4 + d5 * d5);
        float f = (float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d6)));
        float[] fArray = new float[2];
        fArray[0] = mc.player.rotationYaw + MathHelper.wrapDegrees((float)(f - mc.player.rotationYaw));
        fArray[1] = mc.player.rotationPitch + MathHelper.wrapDegrees((float)(f2 - mc.player.rotationPitch));
        return fArray;
    }


    //rhm
    public static float[] shitMethod2(BlockPos blockPos) {
        Vec3d vec3d = vec3dPosition();
        Vec3d vec3d2 = new Vec3d((Vec3i)blockPos).add(0.5, 0.5, 0.5);
        double d = vec3d.squareDistanceTo(vec3d2);
        EnumFacing[] enumFacingArray = EnumFacing.values();
        int n = enumFacingArray.length;
        int n2 = 0;
        if (0 < n) {
            EnumFacing enumFacing = enumFacingArray[n2];
            Vec3d vec3d3 = vec3d2.add(new Vec3d(enumFacing.getDirectionVec()).scale(0.5));
            return shitMethod(vec3d3);
        }
        return shitMethod(vec3d2);
    }

    //rhb
    public static EnumFacing getFacing(BlockPos blockPos) {
        Vec3d vec3d = vec3dPosition();
        Vec3d vec3d2 = new Vec3d((Vec3i)blockPos).add(0.5, 0.5, 0.5);
        double d = vec3d.squareDistanceTo(vec3d2);
        EnumFacing[] enumFacingArray = EnumFacing.values();
        int n = enumFacingArray.length;
        int n2 = 0;
        if (0 < n) {
            EnumFacing enumFacing = enumFacingArray[n2];
            // missing code?
            // по крайней мере ни кфр ни фернфловер тут нихуя не нашли
            return enumFacing;
        }
        return EnumFacing.UP;
    }
}
