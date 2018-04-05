package net.blay09.mods.twitchintegration.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.blay09.mods.chattweaks.ChatViewManager;
import net.blay09.mods.chattweaks.config.gui.ChatTweaksConfigPanel;
import net.blay09.mods.chattweaks.config.gui.ConfigPanelListBase;
import net.blay09.mods.chattweaks.config.gui.ConfigPanelSub;
import net.blay09.mods.chattweaks.config.gui.button.ButtonGeneric;
import net.blay09.mods.chattweaks.config.gui.button.ConfigOptionListenerCallback;
import net.blay09.mods.chattweaks.config.gui.button.ConfigOptionListeners.ButtonListenerListAction;
import net.blay09.mods.chattweaks.config.gui.button.ConfigOptionListeners.ButtonListenerListAction.Type;
import net.blay09.mods.chattweaks.config.gui.button.ConfigOptionListeners.ButtonListenerPanelSelection;
import net.blay09.mods.twitchintegration.LiteModTwitchIntegration;
import net.blay09.mods.twitchintegration.handler.TwitchChannel;
import net.blay09.mods.twitchintegration.handler.TwitchManager;
import net.minecraft.util.text.TextFormatting;

public class ConfigPanelTwitchChannels extends ConfigPanelListBase<TwitchChannel>
{
    private static final Supplier<TwitchChannel> ENTRY_FACTORY = new Supplier<TwitchChannel>()
    {
        @Override
        public TwitchChannel get()
        {
            return new TwitchChannel("twitch");
        }
    };
    private static final ConfigOptionListenerCallback<TwitchChannel> CALLBACK = new ConfigOptionListenerCallback<TwitchChannel>()
    {
        @Override
        public void onListAction(ButtonListenerListAction.Type action, TwitchChannel channel)
        {
            if (channel != null)
            {
                TwitchManager manager = LiteModTwitchIntegration.getTwitchManager();

                if (action == Type.ADD)
                {
                    channel.createOrUpdateChatChannel();
                    manager.addChannel(channel);
                    manager.updateChannelStates();
                    manager.saveChannels();
                    ChatViewManager.save();
                }
                else if (action == Type.REMOVE)
                {
                    manager.removeTwitchChannel(channel);
                    manager.updateChannelStates();
                    manager.saveChannels();
                    ChatViewManager.save();
                }
            }
        }
    };

    private final List<TwitchChannel> list = new ArrayList<>();

    public ConfigPanelTwitchChannels(ChatTweaksConfigPanel parent, ConfigPanelSub parentSubPanel)
    {
        super("Twitch Channels", parent, parentSubPanel, false);
    }

    @Override
    protected List<TwitchChannel> getList()
    {
        return this.list;
    }

    @Override
    public void clearOptions()
    {
        this.list.clear();
        this.list.addAll(LiteModTwitchIntegration.getTwitchManager().getChannels());

        super.clearOptions();
    }

    @Override
    public void saveChanges()
    {
    }

    protected ButtonListenerListAction<ButtonGeneric, TwitchChannel> createActionListener(ButtonListenerListAction.Type type, int index)
    {
        return new ButtonListenerListAction<>(type, index, this.list, ENTRY_FACTORY, this, CALLBACK);
    }

    @Override
    protected void createListEntry(int index, int x, int y, int width, int height)
    {
        if (index < this.list.size() && this.list.get(index) != null)
        {
            TwitchChannel channel = this.list.get(index);
            ConfigPanelSub panelChannelSettings = new ConfigPanelTwitchChannelSettings(this.parentPanel, this, channel);
            ButtonListenerPanelSelection<ButtonGeneric> listener = new ButtonListenerPanelSelection<>(this.parentPanel, panelChannelSettings);
            String label = channel.getName();

            if (channel.isActive() == false)
            {
                label += TextFormatting.DARK_AQUA + " (disabled)" + TextFormatting.RESET;
            }

            this.addButton(new ButtonGeneric(index, x, y, 360, 20, label), listener);
        }
    }
}
