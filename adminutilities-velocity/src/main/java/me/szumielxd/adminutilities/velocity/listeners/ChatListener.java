package me.szumielxd.adminutilities.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.data.ChatPlayer;
import me.szumielxd.adminutilities.common.managers.AdminChatManager.ChannelType;
import me.szumielxd.adminutilities.common.objects.CommonPlayer;

public class ChatListener {
	
	
private final AdminUtilities plugin;
	
	
	public ChatListener(AdminUtilities plugin) {
		this.plugin = plugin;
	}
	
	
	// SAVE CHAT TO CACHE
	@Subscribe(order = PostOrder.NORMAL)
	public void chatSaver(PlayerChatEvent event) {
		if (!event.getResult().isAllowed()) return;
		if (event.getMessage() == null) return;
		
		ChatPlayer cp = ChatPlayer.getChatPlayer(event.getPlayer().getUniqueId());
		if (cp == null) return;
		if (!event.getMessage().startsWith("/")) {
			String[] args = event.getMessage().substring(1).split(" ", 2);
			if (args.length == 0) return;
			if (args[0].isEmpty()) return;
			if (Config.IGNORE_NAMESPACES.getBoolean() && args[0].indexOf(':') >= 0) args[0] = args[0].substring(args[0].indexOf(':') + 1);
			if (!Config.LISTENED_COMMANDS.getStringList().contains(args[0].toLowerCase())) return;
		}
		cp.append(System.currentTimeMillis(), event.getMessage());
	}
	
	
	// Listen AdminChannel
	@Subscribe(order = PostOrder.LATE)
	public void adminChatMonitor(PlayerChatEvent event) {
		
		if (event.getMessage() == null || event.getMessage().isEmpty()) return;
		if (!event.getResult().isAllowed()) return;
		if (event.getMessage().startsWith("/")) return;
		
		CommonPlayer player = this.plugin.getProxyServer().getPlayer(event.getPlayer().getUniqueId());
		ChannelType channel = this.plugin.getAdminChatManager().getChannelByMessage(player, event.getMessage());
		if (channel == null) return;
		if (!this.plugin.hasPermAndNotLobby(channel.getPermission()).test(player)) return;
		this.plugin.getAdminChatManager().sendMessage(channel, player, event.getMessage().substring(channel.getPrefix().length()));
		event.setResult(ChatResult.denied());
	}
	

}
