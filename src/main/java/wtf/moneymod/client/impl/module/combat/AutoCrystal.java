package wtf.moneymod.client.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wtf.moneymod.client.api.events.PacketEvent;
import wtf.moneymod.client.api.management.impl.RotationManagement;
import wtf.moneymod.client.api.setting.annotatable.Bounds;
import wtf.moneymod.client.api.setting.annotatable.Value;
import wtf.moneymod.client.impl.module.Module;
import wtf.moneymod.client.impl.module.global.Global;
import wtf.moneymod.client.impl.utility.impl.math.MathUtil;
import wtf.moneymod.client.impl.utility.impl.misc.Timer;
import wtf.moneymod.client.impl.utility.impl.player.ItemUtil;
import wtf.moneymod.client.impl.utility.impl.render.JColor;
import wtf.moneymod.client.impl.utility.impl.render.Renderer3D;
import wtf.moneymod.client.impl.utility.impl.world.BlockUtil;
import wtf.moneymod.client.impl.utility.impl.world.EntityUtil;
import wtf.moneymod.client.mixin.mixins.ducks.AccessorCPacketPlayer;
import wtf.moneymod.client.mixin.mixins.ducks.AccessorCPacketUseEntity;
import wtf.moneymod.eventhandler.listener.Handler;
import wtf.moneymod.eventhandler.listener.Listener;

import java.util.*;

@Module.Register( label = "AutoCrystal", cat = Module.Category.COMBAT )
public class AutoCrystal extends Module {

    @Value( value = "Place" ) public boolean place = true;
    @Value( value = "Break" ) public boolean hit = true;
    @Value( value = "Logic" ) public Logic logic = Logic.BREAKPLACE;
    @Value( value = "Target Range" ) @Bounds( max = 16 ) public int targetRange = 12;
    @Value( value = "Place Range " ) @Bounds( max = 6 ) public int placeRange = 5;
    @Value( value = "Break Range " ) @Bounds( max = 6 ) public int breakRange = 5;
    @Value( value = "Wall Range" ) @Bounds( max = 6 ) public float wallRange = 3.5f;
    @Value( value = "Break Delay" ) @Bounds( max = 200 ) public int breakDelay = 40;
    @Value( value = "Place Delay" ) @Bounds( max = 200 ) public int placeDelay = 20;
    @Value( value = "MinDamage" ) @Bounds( max = 36 ) public int mindmg = 6;
    @Value( value = "MaxSelfDamage" ) @Bounds( max = 36 ) public int maxselfdamage = 6;
    @Value( value = "FacePlaceDamage" ) @Bounds( max = 36 ) public int faceplacehp = 8;
    @Value( value = "ArmorScale" ) @Bounds( max = 100 ) public int armorscale = 12;
    @Value( value = "TickExisted" ) @Bounds( max = 20 ) public int tickexisted = 3;
    @Value( value = "Predict" ) public boolean boost = true;
    @Value( value = "Test" ) public boolean test = true;
    @Value( value = "Rotate" ) public boolean rotateons = true;
    @Value( value = "Second" ) public boolean secondCheck = true;
    @Value( value = "AutoObbyPlace" ) public boolean autoPlace = true;
    @Value( value = "Swap" ) public Swap swap = Swap.NONE;
    @Value( value = "Swing" ) public Swing swing = Swing.MAINHAND;
    @Value( value = "Color" ) public JColor color = new JColor(255, 0, 0, 180, true);
    @Value( value = "Outline" ) public boolean outlines = false;
    @Value( value = "Box" ) public boolean boxes = true;
    @Value( value = "Line Widht ") @Bounds(max = 3f) public float lineWidht = 0.6f;
    @Value( value = "Expand" ) @Bounds(max = 1f) public float expands = 1;
    private final Set<BlockPos> placeSet = new HashSet<>();
    private BlockPos renderPos, lastPlaced;
    private BlockPos currentBlock;
    public EntityPlayer currentTarget;
    private boolean offhand, rotating, lowArmor, blackPeople;
    private double currentDamage;
    private int ticks, old, autoOld;
    private float yaw = 0f, pitch = 0f;

    private final Timer breakTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Timer predictTimer = new Timer();

    private static AutoCrystal INSTANCE;

    public AutoCrystal() {
        INSTANCE = this;
    }

    public static AutoCrystal getInstance() {
        return INSTANCE;
    }

    @Override
    public void onToggle() {
        autoOld = -1;
        rotating = false;
        blackPeople = false;
        placeSet.clear();
        renderPos = null;
        breakTimer.reset();
        placeTimer.reset();
        predictTimer.reset();
    }

    @Override()
    public void onEnable() {
        if(!nullCheck())autoOld = mc.player.inventory.currentItem;
    }

    @Override
    public void onDisable() {
        if (swap == Swap.AUTO) ItemUtil.swapToHotbarSlot(autoOld, false);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;
        if (ticks++ > 20) {
            ticks = 0;
            placeSet.clear();
            renderPos = null;
        }

        if (swap == Swap.AUTO) {
            int crystal = ItemUtil.findItem(ItemEndCrystal.class);
            if (crystal != -1) ItemUtil.swapToHotbarSlot(crystal, false);
        }
        offhand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
        currentTarget = EntityUtil.getTarget(targetRange);
        if (currentTarget == null){
            renderPos = null;
            return;
        }
        lowArmor = ItemUtil.isArmorLow(currentTarget, ( int ) armorscale);
        doAutoCrystal();
    }

    public void doAutoCrystal() {
        if (logic == Logic.BREAKPLACE) {
            if (hit) this.dbreak();
            if (place) this.place();
        } else {
            if (place) this.place();
            if (hit) this.dbreak();
        }
    }

    public void doHandActive(EnumHand hand) {
        if (hand != null)
            mc.player.setActiveHand(hand);
    }

    @Override public void onRender3D(float partialTicks) {
        if (renderPos != null) {
             Renderer3D.INSTANCE.drawBoxESP(renderPos,color.getColor(),lineWidht,outlines,boxes,color.getColor().getAlpha(), color.getColor().getAlpha(),1);
        }
    }


    private void place() {
        EnumHand hand = null;
        BlockPos placePos = null;
        double maxDamage = 0.5;
        for (BlockPos pos : BlockUtil.INSTANCE.getSphere(placeRange, true)) {
            double selfDamage, targetDamage;

            if (!BlockUtil.canPlaceCrystal(pos, secondCheck) || (targetDamage = EntityUtil.INSTANCE.calculate(( double ) pos.getX() + 0.5, ( double ) pos.getY() + 1.0, ( double ) pos.getZ() + 0.5, currentTarget)) < mindmg && EntityUtil.getHealth(currentTarget) > ( float ) faceplacehp && !lowArmor || (selfDamage = EntityUtil.INSTANCE.calculate(( double ) pos.getX() + 0.5, ( double ) pos.getY() + 1.0, ( double ) pos.getZ() + 0.5, mc.player)) + 2.0 >= maxselfdamage || selfDamage >= targetDamage || maxDamage > targetDamage)
                continue;

            if (currentTarget.isDead)
                continue;
            placePos = pos;
            maxDamage = targetDamage;

        }

        if (placePos == null && autoPlace) {
            maxDamage = 0;
            if (ItemUtil.findItem(Blocks.OBSIDIAN) != -1) {
                if (lastPlaced != null && mc.player.getDistanceSq(lastPlaced) < MathUtil.INSTANCE.square(placeRange) && BlockUtil.canPlaceCrystal(lastPlaced, true) && EntityUtil.INSTANCE.calculate(( double ) lastPlaced.getX() + 0.5, ( double ) lastPlaced.getY() + 1.0, ( double ) lastPlaced.getZ() + 0.5, currentTarget) > mindmg) {
                    placePos = lastPlaced;
                } else {

                    for (BlockPos pos : BlockUtil.INSTANCE.getSphere(placeRange, true)) {
                        double selfDamage, targetDamage;

                        if (!mc.world.getBlockState(pos.up()).getBlock().equals(Blocks.AIR) || !BlockUtil.canPlaceCrystal(pos.up()))
                            continue;

                        mc.world.setBlockState(pos.up(), Blocks.OBSIDIAN.getBlockState().getBaseState());

                        if ((targetDamage = EntityUtil.INSTANCE.calculate(( double ) pos.getX() + 0.5, ( double ) pos.getY() + 2.0, ( double ) pos.getZ() + 0.5, currentTarget)) < mindmg && EntityUtil.getHealth(currentTarget) > ( float ) faceplacehp && !lowArmor || (selfDamage = EntityUtil.INSTANCE.calculate(( double ) pos.getX() + 0.5, ( double ) pos.getY() + 2.0, ( double ) pos.getZ() + 0.5, mc.player)) + 2.0 >= maxselfdamage || selfDamage >= targetDamage || maxDamage > targetDamage) {
                            mc.world.setBlockToAir(pos.up());
                            continue;
                        }

                        mc.world.setBlockToAir(pos.up());

                        if (currentTarget.isDead)
                            continue;
                        placePos = pos.up();
                        lastPlaced = pos.up();
                        maxDamage = targetDamage;

                    }

                    if (placePos != null) {
                        old = mc.player.inventory.currentItem;
                        ItemUtil.swapToHotbarSlot(ItemUtil.findItem(Blocks.OBSIDIAN), false);
                        BlockUtil.INSTANCE.placeBlock(placePos);
                        ItemUtil.swapToHotbarSlot(old, false);
                        blackPeople = true;
                        return;
                    }
                }

            }
        }

        if (swap == Swap.SILENT) {
            if (ItemUtil.findItem(ItemEndCrystal.class) == -1) {
                return;
            }
        }
        if (swap == Swap.NONE) {
            if (!offhand && mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL) return;
        }
        if (swap == Swap.AUTO){
            if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL) return;
        }

        if (maxDamage != 0.5 && placeTimer.passed(( int ) placeDelay)) {
            if(!blackPeople) old = mc.player.inventory.currentItem;
            blackPeople = false;
            if (mc.player.isHandActive()) hand = mc.player.getActiveHand();
            if (swap == Swap.SILENT) ItemUtil.swapToHotbarSlot(ItemUtil.findItem(ItemEndCrystal.class), false);
            if (rotateons) rotate(placePos);
            if (placePos == null) return;
            EnumFacing facing = EnumFacing.UP;
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placePos, facing, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0f, 0f, 0f));
            mc.playerController.updateController();
            if (swap == Swap.SILENT) ItemUtil.swapToHotbarSlot(old, false);
            doHandActive(hand);
            placeSet.add(placePos);
            renderPos = new BlockPos(placePos.getX(), placePos.getY(), placePos.getZ());
            currentBlock = placePos;
            currentDamage = maxDamage;
            placeTimer.reset();

        } else {
            rotating = false;
        }
    }

    private void dbreak() {
        Entity maxCrystal = null;
        double maxDamage = 0.5;
        for (Entity crystal : mc.world.loadedEntityList) {
            double selfDamage, targetDamage;
            if (!(crystal instanceof EntityEnderCrystal)) continue;
            float f = mc.player.canEntityBeSeen(crystal) ? breakRange : wallRange;
            if (!(f > mc.player.getDistance(crystal)) || (targetDamage = EntityUtil.INSTANCE.calculate(crystal.posX, crystal.posY, crystal.posZ, currentTarget)) < mindmg && EntityUtil.getHealth(currentTarget) > ( float ) faceplacehp && !lowArmor || (selfDamage = EntityUtil.INSTANCE.calculate(crystal.posX, crystal.posY, crystal.posZ, mc.player)) + 2.0 >= maxselfdamage || selfDamage >= targetDamage || maxDamage > targetDamage)
                continue;
            maxCrystal = crystal;
            maxDamage = targetDamage;
        }
        if (maxCrystal != null && breakTimer.passed(( int ) breakDelay)) {
            if (!(maxCrystal.ticksExisted >= tickexisted)) return;
            if (rotateons) rotate(maxCrystal);
            mc.getConnection().sendPacket(new CPacketUseEntity(maxCrystal));
            setSwing();
            breakTimer.reset();
        } else {
            rotating = false;
        }
    }

    public void setSwing() {
        if (swing == Swing.MAINHAND) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
        } else if (swing == Swing.OFFHAND) {
            mc.player.swingArm(EnumHand.OFF_HAND);
        } else {}
    }

    @Handler
    public Listener<PacketEvent.Receive> packetEventReceive = new Listener<>(PacketEvent.Receive.class, e -> {
        SPacketSoundEffect packet;
        if (e.getPacket() instanceof SPacketSpawnObject && boost) {
            final SPacketSpawnObject packet2 = e.getPacket();
            EnumHand hand = null;
            if (packet2.getType() == 51 && placeSet.contains(new BlockPos(packet2.getX(), packet2.getY(), packet2.getZ()).down()) && predictTimer.passed(20)) {
                if (mc.player.isHandActive()) hand = mc.player.getActiveHand();
                AccessorCPacketUseEntity hitPacket = ( AccessorCPacketUseEntity ) new CPacketUseEntity();
                int entityId = packet2.getEntityID();
                hitPacket.setEntityId(entityId);
                hitPacket.setAction(CPacketUseEntity.Action.ATTACK);
                mc.getConnection().sendPacket(( CPacketUseEntity ) hitPacket);
                doHandActive(hand);
                setSwing();
                predictTimer.reset();
            }
        }
    });

    @Handler public Listener<PacketEvent.Send> packetEventSend = new Listener<>(PacketEvent.Send.class, e -> {
        if (e.getPacket() instanceof CPacketPlayer && rotating && rotateons) {
            (( AccessorCPacketPlayer ) e.getPacket()).setYaw(yaw);
            (( AccessorCPacketPlayer ) e.getPacket()).setPitch(pitch);
        }
    });

    private void rotate(BlockPos bp) {
        float[] angles = RotationManagement.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(bp.getX() + .5f, bp.getY() + .5f, bp.getZ() + .5f));
        yaw = angles[ 0 ];
        pitch = angles[ 1 ];
        rotating = true;
    }

    private void rotate(Entity e) {
        float[] angles = RotationManagement.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), e.getPositionEyes(mc.getRenderPartialTicks()));
        yaw = angles[ 0 ];
        pitch = angles[ 1 ];
        rotating = true;
    }

    public enum Swap {
        NONE,
        AUTO,
        SILENT
    }

    public enum Swing {
        OFFHAND,
        MAINHAND,
        NONE
    }

    public enum Logic {
        BREAKPLACE,
        PLACEBREAK;
    }

}
