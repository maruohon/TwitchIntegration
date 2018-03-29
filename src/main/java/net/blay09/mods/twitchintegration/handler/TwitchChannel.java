package net.blay09.mods.twitchintegration.handler;

import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import com.google.gson.JsonObject;
import net.blay09.mods.chattweaks.ChatManager;
import net.blay09.mods.chattweaks.ChatViewManager;
import net.blay09.mods.chattweaks.chat.ChatChannel;
import net.blay09.mods.chattweaks.chat.ChatView;
import net.blay09.mods.chattweaks.config.options.IConfigOptionListEntry;
import net.blay09.mods.twitchintegration.reference.Reference;
import net.blay09.mods.twitchintegration.util.TwitchAPI;
import net.minecraft.util.ResourceLocation;

public class TwitchChannel {

	public enum DeletedMessages implements IConfigOptionListEntry<DeletedMessages>
	{
		Show,
		Strikethrough,
		Replace,
		Hide;

		@Override
		public String getStringValue()
		{
			return this.toString();
		}

		@Override
		public String getDisplayName()
		{
			return this.toString();
		}

		@Override
		public int getOrdinalValue()
		{
			return this.ordinal();
		}

		@Override
		public DeletedMessages cycle(boolean forward)
		{
			int id = this.ordinal();

			if (forward)
			{
				if (++id >= values().length)
				{
					id = 0;
				}
			}
			else
			{
				if (--id < 0)
				{
					id = values().length - 1;
				}
			}

			return values()[id % values().length];
		}

		@Override
		public DeletedMessages fromString(String name)
		{
			return fromStringStatic(name);
		}

		public static DeletedMessages fromStringStatic(String name)
		{
			for (DeletedMessages al : DeletedMessages.values())
			{
				if (al.name().equalsIgnoreCase(name))
				{
					return al;
				}
			}

			return DeletedMessages.Replace;
		}

		public static DeletedMessages fromName(String name) {
			try {
				return valueOf(name);
			} catch (IllegalArgumentException e) {
				return Replace;
			}
		}
	}

	private String name;
	private ChatChannel chatChannel;
	private boolean subscribersOnly;
	private DeletedMessages deletedMessages = DeletedMessages.Replace;
	private boolean active = true;
	private int id = -1;
	private Map<String, TwitchBadge> badges;

	public TwitchChannel(String name) {
		this.name = name;
	}

	public void createOrUpdateChatChannel() {
		if(chatChannel != null && !chatChannel.getName().equalsIgnoreCase(name)) {
			ChatManager.removeChatChannel(chatChannel.getName());
		}
		chatChannel = ChatManager.getChatChannel(name);
		if(chatChannel == null) {
			chatChannel = new ChatChannel(name, "Twitch Chat for '" + name + "'", new ResourceLocation(Reference.MOD_ID, "icon.png"));
			ChatManager.addChatChannel(chatChannel);
		}
	}

	public void setName(String name) {
		this.name = name;
		createOrUpdateChatChannel();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public boolean isSubscribersOnly() {
		return subscribersOnly;
	}

	public void setSubscribersOnly(boolean subscribersOnly) {
		this.subscribersOnly = subscribersOnly;
	}

	public DeletedMessages getDeletedMessages() {
		return deletedMessages;
	}

	public void setDeletedMessages(DeletedMessages deletedMessages) {
		this.deletedMessages = deletedMessages;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		if(chatChannel != null) {
			chatChannel.setEnabled(active);
		}
	}

	@Nullable
	public ChatChannel getChatChannel() {
		return chatChannel;
	}

	public JsonObject toJson() {
		JsonObject obj = new JsonObject();
		obj.addProperty("name", name);
		obj.addProperty("subscribersOnly", subscribersOnly);
		obj.addProperty("channelId", id);
		obj.addProperty("deletedMessages", deletedMessages.name());
		obj.addProperty("active", active);
		return obj;
	}

	public static TwitchChannel fromJson(JsonObject obj) {
		TwitchChannel channel = new TwitchChannel(obj.get("name").getAsString());
		channel.subscribersOnly = obj.has("subscribersOnly") && obj.get("subscribersOnly").getAsBoolean();
		channel.id = obj.has("channelId") ? obj.get("channelId").getAsInt() : -1;
		channel.deletedMessages = DeletedMessages.fromName(obj.get("deletedMessages").getAsString());
		channel.active = obj.has("active") && obj.get("active").getAsBoolean();
		return channel;
	}

	public void loadChannelBadges() {
		if(badges == null) {
			new Thread(() -> {
				if (id == -1) {
					id = TwitchAPI.loadChannelId(name);
					if (id == -1) {
						return;
					}
				}
				Map<String, TwitchBadge> badges = TwitchAPI.getDefaultBadges();
				badges.putAll(TwitchAPI.loadChannelSpecificBadges(this));

				this.badges = badges;
			}).start();
		}
	}

	@Nullable
	public TwitchBadge getBadge(String key, int version) {
		if(badges == null) {
			return null;
		}
		TwitchBadge badge = badges.get(key + "/" + version);
		if(badge == null) {
			badge = badges.get(key);
		}
		return badge;
	}

	public void createDefaultView() {
		if(chatChannel != null) {
			ChatView twitchView = new ChatView(name);
			twitchView.addChannel(chatChannel);
			twitchView.setOutgoingPrefix("/twitch #" + name.toLowerCase(Locale.ENGLISH) + " ");
			ChatViewManager.addChatView(twitchView);
			ChatViewManager.save();
		}
	}
}
