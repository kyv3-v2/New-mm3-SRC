package wtf.moneymod.client.mixin.mixins;


import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.moneymod.client.Main;
import wtf.moneymod.client.api.events.*;
import wtf.moneymod.client.impl.module.movement.ElytraFly;

@Mixin( value = EntityPlayerSP.class, priority = 9999 )
public class MixinEntityPlayerSP extends AbstractClientPlayer {

    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Redirect( method = "move", at = @At( value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V" ) )
    public void move(AbstractClientPlayer player, MoverType type, double x, double y, double z) {
        MoveEvent event = new MoveEvent(x, y, z);
        Main.EVENT_BUS.dispatch(event);
        super.move(type, event.motionX, event.motionY, event.motionZ);
    }

    @Inject( method = "onUpdateWalkingPlayer", at = @At( "HEAD" ) )
    public void pre( CallbackInfo info ) {
        UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent( 0 );
        Main.EVENT_BUS.dispatch( event );
    }

    @Inject( method = "onUpdate",
            at = @At( value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V", shift = At.Shift.BEFORE ) )
    public void onPreMotionUpdate( CallbackInfo info )
    {
        MotionUpdateEvent event = new MotionUpdateEvent( Minecraft.getMinecraft( ).player.rotationYaw, Minecraft.getMinecraft( ).player.rotationPitch, Minecraft.getMinecraft( ).player.posX, Minecraft.getMinecraft( ).player.posY, Minecraft.getMinecraft( ).player.posZ, Minecraft.getMinecraft( ).player.onGround, Minecraft.getMinecraft( ).player.noClip, 0 );
        Main.EVENT_BUS.dispatch( event );
    }

    @Redirect( method = "onLivingUpdate", at = @At( value = "INVOKE", target = "Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V" ) )
    public void updatePlayerMoveState( MovementInput input )
    {
        input.updatePlayerMoveState( );
        UpdatePlayerMoveStateEvent event = new UpdatePlayerMoveStateEvent( input );
        Main.EVENT_BUS.dispatch( event );
    }

    @Inject( method = "onUpdate",
            at = @At( value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V", shift = At.Shift.AFTER ) )
    public void onPostMotionUpdate( CallbackInfo info )
    {
        MotionUpdateEvent event = new MotionUpdateEvent( Minecraft.getMinecraft( ).player.rotationYaw, Minecraft.getMinecraft( ).player.rotationPitch,
                Minecraft.getMinecraft( ).player.posX, Minecraft.getMinecraft( ).player.posY, Minecraft.getMinecraft( ).player.posZ, Minecraft.getMinecraft( ).player.onGround, Minecraft.getMinecraft( ).player.noClip, 1 );
        Main.EVENT_BUS.dispatch( event );
    }

    @Inject( method = "onUpdateWalkingPlayer", at = @At( "RETURN" ) )
    public void post( CallbackInfo info ) {
        UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent( 1 );
        Main.EVENT_BUS.dispatch( event );
    }

    @Inject( method = "onUpdate", at = @At( "HEAD" ) )
    public void onUpdatePre( CallbackInfo info )
    {
        PreUpdateEvent event = new PreUpdateEvent( );
        Main.EVENT_BUS.dispatch( event );
    }

    @Override public boolean isElytraFlying() {
        try
        {
            if(Main.getMain().getModuleManager().get(ElytraFly.class).isToggled()) return false;
            return super.isElytraFlying();
        }
        catch( Exception e )
        {
            e.printStackTrace( );
        }
        return false;
    }

}
