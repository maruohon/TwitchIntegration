package net.blay09.mods.twitchintegration.gui;

import java.util.Collection;
import com.google.common.collect.ImmutableList;
import net.blay09.mods.chattweaks.ChatViewManager;
import net.blay09.mods.chattweaks.chat.ChatView;
import net.blay09.mods.chattweaks.config.gui.ChatTweaksConfigPanel;
import net.blay09.mods.chattweaks.config.gui.ConfigPanelSub;
import net.blay09.mods.chattweaks.config.gui.button.ButtonBase;
import net.blay09.mods.chattweaks.config.gui.button.ConfigOptionListeners.ConfigOptionListenerDirtyChecker;
import net.blay09.mods.chattweaks.config.options.ConfigBase;
import net.blay09.mods.chattweaks.config.options.ConfigBoolean;
import net.blay09.mods.chattweaks.config.options.ConfigOptionList;
import net.blay09.mods.chattweaks.config.options.ConfigString;
import net.blay09.mods.twitchintegration.LiteModTwitchIntegration;
import net.blay09.mods.twitchintegration.handler.TwitchChannel;
import net.blay09.mods.twitchintegration.handler.TwitchChannel.DeletedMessages;

public class ConfigPanelTwitchChannelSettings extends ConfigPanelSub
{
    private final TwitchChannel channel;
    private final ConfigOptionListenerDirtyChecker<ButtonBase> listenerDirtyChecker = new ConfigOptionListenerDirtyChecker<>();
    private ConfigString name;
    private ConfigBoolean active;
    private ConfigBoolean subscribersOnly;
    private ConfigOptionList<DeletedMessages> deletedMessages;

    public ConfigPanelTwitchChannelSettings(ChatTweaksConfigPanel parent, ConfigPanelSub parentSubPanel, TwitchChannel channel)
    {
        super("Twitch Channel - " + channel.getName(), parent, parentSubPanel);

        this.channel = channel;
        this.createOptions(channel);
    }

    private void createOptions(TwitchChannel channel)
    {
        this.name               = new ConfigString("Name", channel.getName(), "The name of the Twitch channel");
        this.active             = new ConfigBoolean("Active", channel.isActive(), "Is this channel active/enabled");
        this.subscribersOnly    = new ConfigBoolean("Subscribers Only", channel.isSubscribersOnly(), "If true, then only messages from subscribers\nwill be displayed in the in-game chat");
        this.deletedMessages    = new ConfigOptionList<>("Deleted Messages", channel.getDeletedMessages(), "How deleted messages should be handled");
    }

    @Override
    protected Collection<ConfigBase> getConfigs()
    {
        return ImmutableList.of(
                this.name,
                this.active,
                this.subscribersOnly,
                this.deletedMessages);
    }

    @Override
    public void onPanelHidden()
    {
        boolean dirty = false;

        if (this.listenerDirtyChecker.isDirty())
        {
            dirty = true;
            this.listenerDirtyChecker.resetDirty();
        }

        // This reads the textField contents back to the ConfigBase instances
        dirty |= this.handleTextFields();

        this.channel.setActive(this.active.getValue());
        this.channel.setSubscribersOnly(this.subscribersOnly.getValue());
        this.channel.setDeletedMessages(this.deletedMessages.getValue());

        String oldName = this.channel.getName();

        if (this.name.getStringValue().equalsIgnoreCase(oldName) == false)
        {
            LiteModTwitchIntegration.getTwitchManager().renameTwitchChannel(this.channel, this.name.getStringValue());
        }

        ChatView chatView = ChatViewManager.getChatView(this.channel.getName());

        if (chatView == null)
        {
            this.channel.createDefaultView();
        }

        if (dirty)
        {
            LiteModTwitchIntegration.getTwitchManager().saveChannels();
        }
    }
}
