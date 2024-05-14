package me.szumielxd.adminutilities.common.managers;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
	private final Map<ChannelType, Component> formats = new EnumMap<>(ChannelType.class);
	private final Map<String, ChannelType> channelsByPrefixes = new HashMap<>();
	
	
	public AdminChatManager(AdminUtilities plugin) {
		this.plugin = plugin;
		LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection().toBuilder().hexColors().build();
		for (ChannelType type : ChannelType.values()) {
			try {
				this.formats.put(type, GsonComponentSerializer.gson().deserialize(type.format));
			} catch (Exception e) {
				this.formats.put(type, legacySerializer.deserialize(type.format));
			}
		}
		Stream.of(ChannelType.values())
				.sorted((t1, t2) -> Integer.compare(t1.prefix.length(), t2.prefix.length()))
				.forEach(t -> channelsByPrefixes.put(t.prefix, t));
	}
	
	
	public enum ChannelType {
		
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
			return "adminutilities.admin.chat.channel." + this.name().toLowerCase();
		}
		
		public boolean hasPermission(CommonSender sender) {
			return sender.hasPermission(this.getPermission());
		}
	}
	
	
	public void sendMessage(ChannelType type, CommonSender sender, String message) {
		if (sender.hasPermission(type.getPermission()+".format")) message = MiscUtil.translateAlternateColorCodes('&', message).replaceAll("&(?=#[0-9A-Fa-f]{6})", "§");
		Component comp = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().hexColors().build().deserialize(message);
		UUID uuid = sender instanceof CommonPlayer player ? player.getUniqueId()
				: UUID.fromString("00000000-0000-0000-0000-000000000000");
		this.plugin.getProxyServer().getPlayers().parallelStream()
				.filter(type::hasPermission)
				.forEach(p -> p.sendMessage(Identity.identity(uuid), this.formats.get(type)
						.replaceText(TextReplacementConfig.builder().matchLiteral("{sender}").replacement(MiscUtil.getName(sender)).build())
						.replaceText(TextReplacementConfig.builder().matchLiteral("{senderDisplay}").replacement(MiscUtil.getDisplayName(sender)).build())
						.replaceText(TextReplacementConfig.builder().matchLiteral("{message}").replacement(comp).build())));
	}
	
	
	public Optional<ChannelType> getChannelByMessage(CommonSender sender, String message) {
		for (var entry : this.channelsByPrefixes.entrySet()) {
			if (message.startsWith(entry.getKey())) {
				ChannelType type = entry.getValue();
				if (type.hasPermission(sender)) {
					return Optional.of(type);
				}
			}
		}
		return Optional.empty();
	}
	

}
