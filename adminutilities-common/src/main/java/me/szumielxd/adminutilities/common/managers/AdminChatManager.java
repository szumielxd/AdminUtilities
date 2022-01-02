package me.szumielxd.adminutilities.common.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.objects.CommonPlayer;
import me.szumielxd.adminutilities.common.objects.CommonSender;
import me.szumielxd.adminutilities.common.utils.MiscUtil;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AdminChatManager {
	
	
	private final AdminUtilities plugin;
	private final Map<ChannelType, Component> FORMATS = new HashMap<>();
	private final Map<String, ChannelType> CHANNELS_BY_PREFIXES = new HashMap<>();
	
	
	public AdminChatManager(AdminUtilities plugin) {
		this.plugin = plugin;
		LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection().toBuilder().hexColors().build();
		for (ChannelType type : ChannelType.values()) {
			try {
				this.FORMATS.put(type, GsonComponentSerializer.gson().deserialize(type.format));
			} catch (Exception e) {
				this.FORMATS.put(type, legacySerializer.deserialize(type.format));
			}
		}
		Config.CHANNEL_FORMAT_MOD.getString();
		Stream.of(ChannelType.values()).sorted((t1, t2) -> Integer.compare(t1.prefix.length(), t2.prefix.length())).forEach(t -> CHANNELS_BY_PREFIXES.put(t.prefix, t));
	}
	
	
	public static enum ChannelType {
		MOD(Config.COMMAND_MODCHAT_CHAT_ALIAS.getString(), Config.CHANNEL_FORMAT_MOD.getString()),
		ADMIN(Config.COMMAND_ADMINCHAT_CHAT_ALIAS.getString(), Config.CHANNEL_FORMAT_ADMIN.getString());
		
		
		private final String prefix;
		private final String format;
		private ChannelType(String prefix, String format) {
			this.prefix = prefix;
			this.format = format;
		}
		public String getPrefix() {
			return this.prefix;
		}
		public String getPermission() {
			return "adminutilities.admin.chat.channel."+this.name().toLowerCase();
		}
		public boolean hasPermission(CommonSender sender) {
			return sender.hasPermission(this.getPermission());
		}
	}
	
	
	public void sendMessage(ChannelType type, CommonSender sender, String message) {
		if (sender.hasPermission(type.getPermission()+".format")) message = MiscUtil.translateAlternateColorCodes('&', message).replaceAll("&(?=#[0-9A-Fa-f]{6})", "§");
		Component comp = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().hexColors().build().deserialize(message);
		UUID uuid = sender instanceof CommonPlayer ? ((CommonPlayer)sender).getUniqueId()
				: UUID.fromString("00000000-0000-0000-0000-000000000000");
		this.plugin.getProxyServer().getPlayers().parallelStream().filter(this.plugin.hasPermAndNotLobby(type.getPermission())).forEach(p -> p.sendMessage(Identity.identity(uuid), this.FORMATS.get(type)
				.replaceText(TextReplacementConfig.builder().matchLiteral("{sender}").replacement(MiscUtil.getName(sender)).build())
				.replaceText(TextReplacementConfig.builder().matchLiteral("{senderDisplay}").replacement(MiscUtil.getDisplayName(sender)).build())
				.replaceText(TextReplacementConfig.builder().matchLiteral("{message}").replacement(comp).build())));
	}
	
	
	public ChannelType getChannelByMessage(CommonSender sender, String message) {
		for (String prefix : this.CHANNELS_BY_PREFIXES.keySet()) {
			if (message.startsWith(prefix)) {
				ChannelType type = this.CHANNELS_BY_PREFIXES.get(prefix);
				if (type.hasPermission(sender)) return type;
			}
		}
		return null;
	}
	

}
