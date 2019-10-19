package net.blay09.mods.twitchintegration.gui;

import net.blay09.mods.twitchintegration.reference.Reference;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class GuiTwitchOpenToken extends GuiConfirmOpenLink {
    public GuiTwitchOpenToken(GuiYesNoCallback callback, int i) {
        super(callback, I18n.format(Reference.MOD_ID + ":gui.openToken.requiredPermissions") + "\n" +
                TextFormatting.GRAY + I18n.format(Reference.MOD_ID + ":gui.openToken.logIntoChat") + "\n\n" +
                TextFormatting.RESET + I18n.format(Reference.MOD_ID + ":gui.openToken.openedInBrowser"), i, true);
        messageLine1 = I18n.format(Reference.MOD_ID + ":gui.openToken.authorize");
        disableSecurityWarning();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRenderer, TextFormatting.RED + I18n.format("twitchintegration.gui.no_leak_pls"), width / 2, height / 2 + 50, 0xFFFFFFFF);
    }
}
