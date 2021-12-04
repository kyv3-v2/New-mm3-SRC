package wtf.moneymod.client.impl.module.combat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemShield;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;
import wtf.moneymod.client.Main;
import wtf.moneymod.client.api.management.impl.FriendManagement;
import wtf.moneymod.client.api.management.impl.RotationManagement;
import wtf.moneymod.client.api.setting.annotatable.Bounds;
import wtf.moneymod.client.api.setting.annotatable.Value;
import wtf.moneymod.client.impl.module.Module;
import wtf.moneymod.client.impl.utility.impl.player.ItemUtil;
import wtf.moneymod.client.impl.utility.impl.world.EntityUtil;
import wtf.moneymod.client.mixin.mixins.ducks.AccessorKeyBinding;

@Module.Register( label = "AntiBow", cat = Module.Category.COMBAT)
public class AntiBow extends Module {

    EntityPlayer target;

    @Value(value = "Packet") public boolean packetLook = false;
    @Value(value = "Range") @Bounds(max = 40) public int range = 40;
    @Value(value = "CheckUse") public boolean checkUse = false;
    @Value(value = "MaxUse") @Bounds(max = 20) public int maxUse = 10;

    int old;
    boolean b;

    @Override
    public void onToggle() {
        b = false;
        old = -1;
        target = null;
    }

    @Override
    public void onTick() {
        target = EntityUtil.getTarget(range);

        if (target == null) {
            if (b) {
                ((AccessorKeyBinding) mc.gameSettings.keyBindUseItem).setPressed(false);
                if (old != -1) ItemUtil.swapToHotbarSlot(old, false);
                target = null;
                b = false;
            }
            return;
        } else {
            old = mc.player.inventory.currentItem;
            int shield = ItemUtil.findItem(ItemShield.class);
            if (shield == -1){ target = null; return; }
            if (FriendManagement.getInstance().is(target.getName())) return;
            if (checkUse) if (target.getItemInUseMaxCount() <= maxUse) return;
            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemShield) {
                ((AccessorKeyBinding) mc.gameSettings.keyBindUseItem).setPressed(true);
                ItemUtil.swapToHotbarSlot(shield, false);
                Main.getMain().getRotationManagement().look(target, packetLook);
                b = true;
            }
        }
    }
}
