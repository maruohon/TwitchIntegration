package net.blay09.mods.twitchintegration.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mumfrey.liteloader.core.LiteLoader;
import net.blay09.mods.chattweaks.LiteModChatTweaks;
import net.blay09.mods.chattweaks.auth.TokenPair;
import net.blay09.mods.chattweaks.config.options.ConfigBase;
import net.blay09.mods.chattweaks.config.options.ConfigBoolean;
import net.blay09.mods.chattweaks.config.options.ConfigInteger;
import net.blay09.mods.chattweaks.config.options.ConfigStringList;
import net.blay09.mods.chattweaks.util.JsonUtils;
import net.blay09.mods.twitchintegration.LiteModTwitchIntegration;
import net.blay09.mods.twitchintegration.reference.Reference;

public class Configs
{
    private static final String CONFIG_FILE_NAME = Reference.MOD_ID + ".json";

    public static class Twitch
    {
        public static final ConfigInteger PORT                  = new ConfigInteger("port", 6667, "The port to connect to on the twitch chat server");
        public static final ConfigBoolean DISABLE_NAME_BADGES   = new ConfigBoolean("disableNameBadges", false, "Disable username badges");
        public static final ConfigBoolean DISABLE_USER_COLORS   = new ConfigBoolean("disableUserColors", false, "Disable username colors");
        public static final ConfigBoolean DONT_STORE_TOKEN      = new ConfigBoolean("dontStoreToken", false, "Set this if you're on a public computer or concerned about security.\nYou will have to re-authenticate every time you start Minecraft.");
        public static final ConfigBoolean SHOW_WHISPERS         = new ConfigBoolean("showWhispers", false, "Show whispers in the game chat");
        public static final ConfigBoolean USE_ANONYMOUS_LOGIN   = new ConfigBoolean("useAnonymousLogin", false, "If you login anonymously you can read chat, but you will not\nbe able to type to Twitch chat from within Minecraft");
        public static final ConfigStringList USER_BLACKLIST     = new ConfigStringList("userBlackList", ImmutableList.of("nightbot"), "Messages by these users will not display in chat. Useful to hide bots for example.");

        public static final ImmutableList<ConfigBase> OPTIONS = ImmutableList.of(
                DISABLE_NAME_BADGES,
                DISABLE_USER_COLORS,
                DONT_STORE_TOKEN,
                SHOW_WHISPERS,
                USE_ANONYMOUS_LOGIN,
                USER_BLACKLIST,
                PORT
                );
    }

    public static void load()
    {
        File configFile = new File(LiteLoader.getCommonConfigFolder(), CONFIG_FILE_NAME);

        if (configFile.exists() && configFile.isFile() && configFile.canRead())
        {
            JsonElement element = JsonUtils.parseJsonFile(configFile);

            if (element != null && element.isJsonObject())
            {
                JsonObject root = element.getAsJsonObject();

                net.blay09.mods.chattweaks.config.Configs.readOptions(root, "Twitch", Twitch.OPTIONS);
            }
        }

        if (Twitch.DONT_STORE_TOKEN.getValue())
        {
            TokenPair token = LiteModChatTweaks.getAuthManager().getToken(Reference.MOD_ID);

            if (token != null)
            {
                LiteModChatTweaks.getAuthManager().storeToken(Reference.MOD_ID, token.getUsername(), token.getToken(), true);
            }
        }
    }

    public static void save()
    {
        File dir = LiteLoader.getCommonConfigFolder();

        if (dir.exists() && dir.isDirectory())
        {
            File configFile = new File(dir, CONFIG_FILE_NAME);
            FileWriter writer = null;
            JsonObject root = new JsonObject();

            net.blay09.mods.chattweaks.config.Configs.writeOptions(root, "Twitch", Twitch.OPTIONS);

            try
            {
                writer = new FileWriter(configFile);
                writer.write(JsonUtils.GSON.toJson(root));
                writer.close();
            }
            catch (IOException e)
            {
                LiteModTwitchIntegration.logger.warn("Failed to write configs to file '{}'", configFile.getAbsolutePath(), e);
            }
            finally
            {
                try
                {
                    if (writer != null)
                    {
                        writer.close();
                    }
                }
                catch (Exception e)
                {
                    LiteModTwitchIntegration.logger.warn("Failed to close config file", e);
                }
            }
        }
    }
}
