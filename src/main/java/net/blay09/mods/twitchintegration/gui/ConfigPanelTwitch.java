package net.blay09.mods.twitchintegration.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;
import net.blay09.mods.chattweaks.LiteModChatTweaks;
import net.blay09.mods.chattweaks.config.gui.ChatTweaksConfigPanel;
import net.blay09.mods.chattweaks.config.gui.ConfigPanelSub;
import net.blay09.mods.chattweaks.config.gui.button.ButtonActionListener;
import net.blay09.mods.chattweaks.config.gui.button.ButtonBase;
import net.blay09.mods.chattweaks.config.gui.button.ButtonGeneric;
import net.blay09.mods.chattweaks.config.gui.button.ConfigOptionListeners.ButtonListenerPanelSelection;
import net.blay09.mods.chattweaks.config.options.ConfigBase;
import net.blay09.mods.chattweaks.config.options.ConfigStringList;
import net.blay09.mods.twitchintegration.LiteModTwitchIntegration;
import net.blay09.mods.twitchintegration.config.Configs;
import net.blay09.mods.twitchintegration.handler.TwitchChannel;
import net.blay09.mods.twitchintegration.handler.TwitchManager;
import net.blay09.mods.twitchintegration.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class ConfigPanelTwitch extends ConfigPanelSub
{
    private final TwitchManager manager;
    private ButtonGeneric connectButton;
    private boolean connectedLast;

    public ConfigPanelTwitch(ChatTweaksConfigPanel parent)
    {
        super("Twitch Integration", parent, null);

        this.manager = LiteModTwitchIntegration.getTwitchManager();
    }

    @Override
    protected Collection<ConfigBase> getConfigs()
    {
        return Configs.Twitch.OPTIONS;
    }

    @Override
    public void onPanelHidden()
    {
        boolean dirty = false;

        if (this.getButtonListener().isDirty())
        {
            dirty = true;
            this.getButtonListener().resetDirty();
        }

        dirty |= this.handleTextFields();

        if (dirty)
        {
            Configs.save();
        }
    }

    @Override
    public void addOptions(ConfigPanelHost host)
    {
        super.addOptions(host);

        List<String> list = new ArrayList<>();

        for (TwitchChannel channel : this.manager.getChannels())
        {
            if (channel != null)
            {
                list.add(channel.getName());
            }
        }

        ConfigPanelSub panelChannels = new ConfigPanelTwitchChannels(this.parentPanel, this);
        ButtonListenerPanelSelection<ButtonGeneric> listenerPanelSelection = new ButtonListenerPanelSelection<>(this.parentPanel, panelChannels);

        String label;

        if (list.isEmpty())
        {
            label = TextFormatting.RED + I18n.format("chattweaks:gui.error.button.chat_views.no_channels") + TextFormatting.RESET;
        }
        else
        {
            label = ConfigStringList.getClampedDisplayStringOf(list, 34, "Channels [ ", " ]");
        }

        int id = this.getConfigs().size();
        int x = 10;
        int labelWidth = this.getMaxLabelWidth(this.getConfigs()) + 10;

        // Add the Channels configuration button
        this.addButton(new ButtonGeneric(id++, x + labelWidth, (this.nextElementY += 21), 204, 20, label), listenerPanelSelection);

        // Add the Edit Authentication button
        boolean isAuthenticated = LiteModChatTweaks.getAuthManager().getToken(Reference.MOD_ID) != null;
        boolean isConnected = this.manager.isConnected();
        label = I18n.format(isAuthenticated ? "twitchintegration.config.edit_authentication" : "twitchintegration.config.authenticate");

        if (isAuthenticated == false && Configs.Twitch.USE_ANONYMOUS_LOGIN.getValue() == false)
        {
            label = TextFormatting.GREEN + label;
        }

        ButtonListenerEditAuthentication<ButtonGeneric> listenerAuth = new ButtonListenerEditAuthentication<>(this.mc.currentScreen);
        this.addButton(new ButtonGeneric(id++, x + labelWidth, (this.nextElementY += 21), 204, 20, label), listenerAuth);

        // Add the Connect / Disconnect button
        ButtonListenerConnect<ButtonGeneric> listenerConnect = new ButtonListenerConnect<>(this.manager);
        label = getConnectButtonDisplayString(this.manager);
        this.connectButton = new ButtonGeneric(id++, x + labelWidth, (this.nextElementY += 21), 204, 20, label);
        this.connectButton.enabled = isAuthenticated || isConnected;
        this.addButton(this.connectButton, listenerConnect);
    }

    @Override
    public void onTick(ConfigPanelHost host)
    {
        super.onTick(host);

        boolean connected = this.manager.isConnected();

        if (connected != this.connectedLast && this.connectButton != null)
        {
            this.connectButton.displayString = getConnectButtonDisplayString(this.manager);
            this.connectedLast = connected;
        }
    }

    public static String getConnectButtonDisplayString(TwitchManager manager)
    {
        boolean isAuthenticated = LiteModChatTweaks.getAuthManager().getToken(Reference.MOD_ID) != null;
        boolean canConnect = isAuthenticated || Configs.Twitch.USE_ANONYMOUS_LOGIN.getValue();
        boolean isConnected = manager.isConnected();
        String pre = (isConnected || canConnect == false) ? TextFormatting.RED.toString() : TextFormatting.GREEN.toString();
        String text = I18n.format("twitchintegration:gui.authentication." + (isConnected ?  "disconnect" : "connect"));
        return pre + text + TextFormatting.RESET;
    }

    public static class ButtonListenerConnect<T extends ButtonBase> implements ButtonActionListener<T>
    {
        private final TwitchManager manager;

        public ButtonListenerConnect(TwitchManager manager)
        {
            this.manager = manager;
        }

        @Override
        public void actionPerformed(T control)
        {
            if (this.manager.isConnected())
            {
                this.manager.disconnect();
            }
            else
            {
                this.manager.connect();
            }

            control.displayString = ConfigPanelTwitch.getConnectButtonDisplayString(this.manager);
        }

        @Override
        public void actionPerformedWithButton(T control, int mouseButton)
        {
            this.actionPerformed(control);
        }
    }

    public static class ButtonListenerEditAuthentication<T extends ButtonBase> implements ButtonActionListener<T>
    {
        private final GuiScreen parentScreen;

        public ButtonListenerEditAuthentication(GuiScreen parentScreen)
        {
            this.parentScreen = parentScreen;
        }

        @Override
        public void actionPerformed(T control)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiTwitchAuthentication(this.parentScreen));
        }

        @Override
        public void actionPerformedWithButton(T control, int mouseButton)
        {
            this.actionPerformed(control);
        }
    }
}
