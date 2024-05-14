package me.szumielxd.adminutilities.velocity.listeners;

import java.util.Optional;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;
import com.velocitypowered.api.proxy.Player;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.data.ChatPlayer;

public class ChatListener {
	
	
private final AdminUtilities plugin;
	
	
	public ChatListener(AdminUtilities plugin) {
		this.plugin = plugin;
	}
	
	// SAVE CHAT TO CACHE
	@Subscribe(order = PostOrder.LAST)
	public void commandSaver(PlayerChatEvent event) {
		if (event.getResult().isAllowed()) {
			Optional.ofNullable(ChatPlayer.getChatPlayer(event.getPlayer().getUniqueId())).ifPresent(cp -> {
				cp.append(System.currentTimeMillis(), event.getMessage());
			});
		}
	}
	
	// SAVE COMMANDS TO CACHE
	@Subscribe(order = PostOrder.LAST)
	public void commandSaver(CommandExecuteEvent event) {
		if (event.getResult().isAllowed() && event.getCommandSource() instanceof Player player) {
			Optional.ofNullable(ChatPlayer.getChatPlayer(player.getUniqueId())).ifPresent(cp -> {
				String[] args = event.getCommand().split(" ", 2);
				if (Config.IGNORE_NAMESPACES.getBoolean() && args[0].indexOf(':') >= 0) {
					args[0] = args[0].substring(args[0].indexOf(':') + 1);
				}
				if (Config.LISTENED_COMMANDS.getStringList().contains(args[0].toLowerCase())) {
					cp.append(System.currentTimeMillis(), '/' + event.getCommand());
				}
			});
		}
	}
	
	
	// Listen AdminChannel
	@Subscribe(order = PostOrder.LATE)
	public void adminChatMonitor(PlayerChatEvent event) {
		if (event.getResult().isAllowed()) {
			Optional.ofNullable(this.plugin.getProxyServer().getPlayer(event.getPlayer().getUniqueId())).ifPresent(player -> 
					this.plugin.getAdminChatManager().getChannelByMessage(player, event.getMessage()).ifPresent(channel -> {
							this.plugin.getAdminChatManager().sendMessage(channel, player, event.getMessage().substring(channel.getPrefix().length()));
							event.setResult(ChatResult.denied());
					}));
		}
	}
	

}
