package wtf.moneymod.client.impl.ui.click.buttons.settings.sub;

import net.minecraft.client.gui.Gui;
import wtf.moneymod.client.impl.ui.click.Component;
import wtf.moneymod.client.impl.ui.click.Screen;
import wtf.moneymod.client.impl.ui.click.buttons.settings.ModeButton;
import wtf.moneymod.client.impl.utility.impl.misc.SettingUtils;

import java.awt.*;

public class SubMode extends Component {

    final ModeButton modeButton;
    final String mode;
    int offset;

    public SubMode(ModeButton modeButton, String mode, int offset ) {
        this.mode = mode;
        this.offset = offset;
        this.modeButton = modeButton;
    }

    @Override public void render( int mouseX, int mouseY ) {
        Screen.abstractTheme.drawSubModeButton(this, mode, modeButton.getButton( ).panel.getX( ), modeButton.getButton( ).panel.getY( ) + modeButton.getOffset( ) + this.offset, modeButton.getButton().panel.getWidth(), 12, isHover(mouseX, mouseY) );
    }

    @Override public void mouseClicked( double mouseX, double mouseY, int button ) {
        super.mouseClicked( mouseX, mouseY, button );
        if ( isHover( mouseX, mouseY ) && this.modeButton.getButton( ).open && this.modeButton.isOpen( ) && button == 0 ) {
            modeButton.getSetting( ).setValue( SettingUtils.INSTANCE.getProperEnum(modeButton.getSetting().getValue(), mode) );
        }
    }

    private boolean isHover( final double x, final double y ) {
        return x > modeButton.getX( ) && x < modeButton.getX( ) + 110 && y > modeButton.getY( ) + offset && y < modeButton.getY( ) + 12 + offset;
    }

    public int getOffset( ) {
        return offset;
    }

    public String getMode() {
        return mode;
    }

    public ModeButton getModeButton() {
        return modeButton;
    }

}
