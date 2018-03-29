package net.blay09.mods.twitchintegration;

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mumfrey.liteloader.InitCompleteListener;
import com.mumfrey.liteloader.JoinGameListener;
import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.core.LiteLoader;
import net.blay09.mods.chattweaks.chat.emotes.twitch.BTTVChannelEmotes;
import net.blay09.mods.chattweaks.chat.emotes.twitch.FFZChannelEmotes;
import net.blay09.mods.chattweaks.config.gui.ChatTweaksConfigPanel;
import net.blay09.mods.twitchintegration.command.ClientCommandHandler;
import net.blay09.mods.twitchintegration.command.CommandTwitch;
import net.blay09.mods.twitchintegration.config.Configs;
import net.blay09.mods.twitchintegration.gui.ConfigPanelTwitch;
import net.blay09.mods.twitchintegration.handler.TwitchChannel;
import net.blay09.mods.twitchintegration.handler.TwitchChatHandler;
import net.blay09.mods.twitchintegration.handler.TwitchManager;
import net.blay09.mods.twitchintegration.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.INetHandler;
import net.minecraft.network.play.server.SPacketJoinGame;

public class LiteModTwitchIntegration implements LiteMod, InitCompleteListener, JoinGameListener
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);
    private String configDirPath;
    private TwitchManager twitchManager;
    private TwitchChatHandler twitchChatHandler;
    private static LiteModTwitchIntegration instance;

    public LiteModTwitchIntegration()
    {
        instance = this;
    }

    public static LiteModTwitchIntegration getInstance()
    {
        return instance;
    }

    @Override
    public String getName()
    {
        return Reference.MOD_NAME;
    }

    @Override
    public String getVersion()
    {
        return Reference.MOD_VERSION;
    }

    @Override
    public void init(File configPath)
    {
        this.configDirPath = new File(LiteLoader.getCommonConfigFolder(), Reference.MOD_ID).getAbsolutePath();

        File configDir = new File(this.configDirPath);

        if (configDir.exists() == false && configDir.mkdirs() == false)
        {
            logger.error("Failed to create Twitch Integration config directory");
        }

        Configs.load();
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath)
    {
    }

    @Override
    public void onInitCompleted(Minecraft minecraft, LiteLoader loader)
    {
        this.twitchManager = new TwitchManager(new File(this.configDirPath, "twitch_channels.json"));
        this.twitchChatHandler = new TwitchChatHandler(this.twitchManager);
        ChatTweaksConfigPanel.registerSubPanelFactory(parent -> { return new ConfigPanelTwitch(parent); });
        ClientCommandHandler.INSTANCE.registerCommand(new CommandTwitch());
    }

    @Override
    public void onJoinGame(INetHandler netHandler, SPacketJoinGame joinGamePacket, ServerData serverData, RealmsServer realmsServer)
    {
        if (this.twitchManager.isConnected() == false)
        {
            this.twitchManager.connect();
        }
    }

    public static TwitchChatHandler getTwitchChatHandler()
    {
        return instance.twitchChatHandler;
    }

    public static TwitchManager getTwitchManager()
    {
        return instance.twitchManager;
    }

    // Let's just put this here for now...
    public static void loadChannelEmotes(TwitchChannel channel)
    {
        new Thread(() -> {
            if (net.blay09.mods.chattweaks.config.Configs.Emotes.BTTV_CHANNEL_EMOTES.getValue())
            {
                try {
                    new BTTVChannelEmotes(channel.getName());
                } catch (Exception e) {
                    logger.error("Failed to load BTTV channel emotes: ", e);
                }
            }

            if (net.blay09.mods.chattweaks.config.Configs.Emotes.FFZ_CHANNEL_EMOTES.getValue())
            {
                try {
                    new FFZChannelEmotes(channel.getName());
                } catch (Exception e) {
                    logger.error("Failed to load FFZ channel emotes: ", e);
                }
            }
        }).start();
    }
}
