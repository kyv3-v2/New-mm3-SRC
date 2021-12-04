package wtf.moneymod.client.impl.ui.click.theme.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.gui.Gui;
import org.apache.commons.codec.digest.DigestUtils;
import org.lwjgl.input.Keyboard;
import wtf.moneymod.client.Main;
import wtf.moneymod.client.api.setting.Option;
import wtf.moneymod.client.impl.command.impl.CryptCommand;
import wtf.moneymod.client.impl.module.global.ClickGui;
import wtf.moneymod.client.impl.ui.click.Panel;
import wtf.moneymod.client.impl.ui.click.Screen;
import wtf.moneymod.client.impl.ui.click.buttons.ModuleButton;
import wtf.moneymod.client.impl.ui.click.buttons.settings.ColorButton;
import wtf.moneymod.client.impl.ui.click.buttons.settings.KeyButton;
import wtf.moneymod.client.impl.ui.click.buttons.settings.sub.SubMode;
import wtf.moneymod.client.impl.ui.click.theme.AbstractTheme;
import wtf.moneymod.client.impl.utility.impl.misc.SettingUtils;
import wtf.moneymod.client.impl.utility.impl.render.ColorUtil;
import wtf.moneymod.client.impl.utility.impl.render.JColor;
import wtf.moneymod.client.impl.utility.impl.render.fonts.FontRender;

import java.awt.*;

public class NodusTheme extends AbstractTheme {

    private static final NodusTheme INSTANCE = new NodusTheme();

    public static NodusTheme getInstance() {
        return INSTANCE;
    }

    @Override public void drawHeader(Panel panel, int x, int y, int w, int h, boolean hovered) {
        drawRect(x - 2, y - 2, x + w + 2, y, new Color(255, 255, 255, 90).getRGB());
        drawRect(x, y, x + w, y + h - 2, new Color(0, 0, 0, 210).getRGB());
        if (panel.isOpen())
            Gui.drawRect(x, y + h - 2, x + w, y + h, new Color(255, 255, 255, 90).getRGB());
        FontRender.drawStringWithShadow(CryptCommand.crypting ? ChatFormatting.OBFUSCATED + panel.category.name() + ChatFormatting.RESET : panel.category.name() + ChatFormatting.WHITE + " (" + panel.components.size() + ")", x + 3, y + 4, Screen.color.getRGB());
        FontRender.drawStringWithShadow(panel.isOpen() ? "-" : "+", x + w - 10, y + 4, Color.GRAY.getRGB());
    }

    @Override public void drawPanelOutline(Panel panel, int x, int y, int w, int h, boolean hovered) {
        int localHeight = panel.isOpen() ? h : 18 - 2;
        drawRect(x - 2, y, x, y + localHeight, new Color(255, 255, 255, 90).getRGB());
        drawRect(x + w, y, x + w + 2, y + localHeight, new Color(255, 255, 255, 90).getRGB());
        drawRect(x - 2, y + localHeight, x + w + 2, y + localHeight + 2, new Color(255, 255, 255, 90).getRGB());
    }

    @Override public void drawModuleButton(ModuleButton module, int x, int y, int w, int h, boolean hovered) {
        drawRect(x, y, x + w, y + h, hovered ? new Color(0, 0, 0, 160).getRGB() : new Color(0, 0, 0, 140).getRGB());
        FontRender.drawStringWithShadow(CryptCommand.crypting ? ChatFormatting.OBFUSCATED + module.module.getLabel() + ChatFormatting.RESET  : module.module.getLabel(), x + 3, y + ((( ClickGui ) Main.getMain().getModuleManager().get(ClickGui.class)).bounding ? (hovered ? 1 : 2) : 2), module.module.isToggled() ? Screen.color.getRGB() : -1);

        String openText;
        if (module.open) {
            openText = "-";
        } else openText = "+";

        FontRender.drawStringWithShadow(openText, x + w - 10, y + 2, -1);
    }

    @Override public void drawBooleanButton(Option<Boolean> container, int x, int y, int w, int h, boolean hovered) {
        drawRect(x, y, x + w, y + 12, hovered ? new Color(0, 0, 0, 160).getRGB() : new Color(0, 0, 0, 140).getRGB());
        FontRender.drawStringWithShadow(container.getName(), x + 5, y + ((( ClickGui ) Main.getMain().getModuleManager().get(ClickGui.class)).bounding ? (hovered ? 1 : 2) : 2), container.getValue() ? (( ClickGui ) Main.getMain().getModuleManager().get(ClickGui.class)).color.getColor().getRGB() : new Color(160, 160, 160).getRGB());
    }

    @Override
    public void drawSliderButton(Option<Number> container, int x, int y, int w, int h, double sliderWidth, boolean hovered) {
        Gui.drawRect(x, y, x + w, y + h, hovered ? new Color(0, 0, 0, 160).getRGB() : new Color(0, 0, 0, 140).getRGB());
        Gui.drawRect(x + 3, y + h - 1, ( int ) (x + 3 + sliderWidth), y + 10, hovered ? Screen.color.darker().getRGB() : Screen.color.getRGB());
        FontRender.drawStringWithShadow(container.getName() + ": " + ChatFormatting.GRAY + container.getValue(), x + 5, y + ((( ClickGui ) Main.getMain().getModuleManager().get(ClickGui.class)).bounding ? (hovered ? 0 : 1.5f) : 1.5f), hovered ? new Color(170, 170, 170).getRGB() : -1);
    }

    @Override public void drawKeyButton(KeyButton button, int x, int y, int w, int h, boolean hovered) {
        Gui.drawRect(x, y, x + w, y + h, hovered ? new Color(0, 0, 0, 160).getRGB() : new Color(0, 0, 0, 140).getRGB());
        if(hovered) {
            FontRender.drawStringWithShadow(button.button.module.isHold() ? "Hold" : "Toggle", x + 5, y + 2, -1);
        } else {
            FontRender.drawStringWithShadow("Key", x + 5, y + 2, -1);
        }
        if (button.isBinding()) {
            FontRender.drawStringWithShadow("...", x + w - 5 - mc.fontRenderer.getStringWidth("..."), y + ((( ClickGui ) Main.getMain().getModuleManager().get(ClickGui.class)).bounding ? (hovered ? 1 : 2) : 2), -1);
        } else {
            String key;
            switch (button.button.module.getKey()) {
                case 345:
                    key = "RCtrl";
                    break;
                case 341:
                    key = "Ctrl";
                    break;
                case 346:
                    key = "RAlt";
                    break;
                case -1:
                    key = "NONE";
                    break;
                default:
                    key = Keyboard.getKeyName(button.button.module.getKey());
            }

            FontRender.drawStringWithShadow(key, x + w - 5 - mc.fontRenderer.getStringWidth(key), y + ((( ClickGui ) Main.getMain().getModuleManager().get(ClickGui.class)).bounding ? (hovered ? 1 : 2) : 2), -1);
        }
    }

    @Override public void drawModeButton(Option<Enum> container, int x, int y, int w, int h, boolean hovered) {
        Gui.drawRect(x, y, x + w, y + h, hovered ? new Color(0, 0, 0, 160).getRGB() : new Color(0, 0, 0, 140).getRGB());
        FontRender.drawStringWithShadow(container.getName(), x + 5, y + 2, -1);
        FontRender.drawStringWithShadow(SettingUtils.INSTANCE.getProperName(container.getValue()), x + w - 5 - mc.fontRenderer.getStringWidth(SettingUtils.INSTANCE.getProperName(container.getValue())), y + ((( ClickGui ) Main.getMain().getModuleManager().get(ClickGui.class)).bounding ? (hovered ? 1 : 2) : 2), -1);
    }

    @Override public void drawSubModeButton(SubMode container, String current, int x, int y, int w, int h, boolean hovered) {
        Gui.drawRect(x, y, x + w, y + h, hovered ? new Color(0, 0, 0, 160).getRGB() : new Color(0, 0, 0, 140).getRGB());
        FontRender.drawStringWithShadow(current, (x + w / 2f) - mc.fontRenderer.getStringWidth(current) / 2f, y + 2, SettingUtils.INSTANCE.getProperName(container.getModeButton().getSetting().getValue()).equalsIgnoreCase(current) ? Screen.color.getRGB() : -1);
    }

    @Override public void drawColorButton(Option<JColor> container, int x, int y, int w, int h, boolean hovered) {
        Gui.drawRect(x, y, x + w, y + h, hovered ? new Color(0, 0, 0, 160).getRGB() : new Color(0, 0, 0, 140).getRGB());

        drawVGradientRect(x + w - h,
                y + 2,
                x + w - 4,
                y + 10,
                ColorUtil.injectAlpha((( JColor ) container.getValue()).getColor(), 255).getRGB(), (( JColor ) container.getValue()).getColor().getRGB());

        drawOutline(x + w - h,
                y + 2,
                x + w - 4,
                y + 10,
                1f, new Color(255, 255, 255, 90).getRGB());

        FontRender.drawStringWithShadow(container.getName(), x + 5, y + ((( ClickGui ) Main.getMain().getModuleManager().get(ClickGui.class)).bounding ? (hovered ? 1 : 2) : 2), -1);
    }

    @Override public void drawPickerButton(ColorButton container, int x, int y, int w, int h, boolean hovered) {
        float[] hsb = new float[] {
                Color.RGBtoHSB(container.setting.getValue().getColor().getRed(), container.setting.getValue().getColor().getGreen(), container.setting.getValue().getColor().getBlue(), null)[ 0 ],
                Color.RGBtoHSB(container.setting.getValue().getColor().getRed(), container.setting.getValue().getColor().getGreen(), container.setting.getValue().getColor().getBlue(), null)[ 1 ],
                Color.RGBtoHSB(container.setting.getValue().getColor().getRed(), container.setting.getValue().getColor().getGreen(), container.setting.getValue().getColor().getBlue(), null)[ 2 ]
        };

        int alphas = (( JColor ) container.setting.getValue()).getColor().getAlpha();

        int selectedColor = Color.HSBtoRGB(hsb[ 0 ], 1.0f, 1.0f);

        float selectedRed = (selectedColor >> 16 & 0xFF) / 255.0f;
        float selectedGreen = (selectedColor >> 8 & 0xFF) / 255.0f;
        float selectedBlue = (selectedColor & 0xFF) / 255.0f;
        Gui.drawRect(x, y + 12, x + 110, y + 150, new Color(0, 0, 0, 140).getRGB());
        drawPickerBase(x + 2,
                y + 18,
                106,
                108,
                selectedRed,
                selectedGreen,
                selectedBlue,
                1f);

        drawHueSlider(x + 2,
                y + 127,
                105,
                6,
                hsb[ 0 ]);

        drawAlphaSlider(x + 2,
                y + 134,
                105,
                6,
                selectedRed,
                selectedGreen,
                selectedBlue,
                alphas / 255f);

        float xPos = x + 2 + hsb[ 1 ] * 106,
                yPos = (y + 18 + 108) - hsb[ 2 ] * 108;


        pushMatrix();
        drawPolygonOutline(xPos - 2, yPos + 2, 2f, 3, 360, Color.black.getRGB());
        popMatrix();

        FontRender.drawStringWithShadow("Rainbow", ( int ) (x + w / 2f - mc.fontRenderer.getStringWidth("Rainbow") / 2f), y + h, (( JColor ) container.setting.getValue()).isRainbow() ? (( JColor ) container.setting.getValue()).getColor().getRGB() : -1);


    }

}
