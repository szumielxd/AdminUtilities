package me.szumielxd.adminutilities.bungee.listeners;

import java.util.Optional;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.data.ChatPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ChatListener implements Listener {
	
	
	private final AdminUtilities plugin;
	
	
	public ChatListener(AdminUtilities plugin) {
		this.plugin = plugin;
	}
	
	
	// SAVE CHAT TO CACHE
	@EventHandler(priority = EventPriority.NORMAL)
	public void chatSaver(ChatEvent event) {
		if (!event.isCancelled() && event.getSender() instanceof ProxiedPlayer pPlayer) {
			ChatPlayer cp = ChatPlayer.getChatPlayer(pPlayer.getUniqueId());
			if(cp == null) return;
			if(event.isCommand()) {
				String[] args = event.getMessage().substring(1).split(" ", 2);
				if (Config.IGNORE_NAMESPACES.getBoolean() && args[0].indexOf(':') >= 0) {
					args[0] = args[0].substring(args[0].indexOf(':') + 1);
				}
				if (!Config.LISTENED_COMMANDS.getStringList().contains(args[0].toLowerCase())) {
					return;
				}
			}
			cp.append(System.currentTimeMillis(), event.getMessage());
		}
	}
	
	
	// Listen AdminChannel
	@EventHandler(priority = EventPriority.HIGH)
	public void adminChatMonitor(ChatEvent event) {
		if (!event.isCancelled() && !event.isCommand() && event.getSender() instanceof ProxiedPlayer pPlayer) {
			Optional.ofNullable(this.plugin.getProxyServer().getPlayer(pPlayer.getUniqueId())).ifPresent(player -> 
					this.plugin.getAdminChatManager().getChannelByMessage(player, event.getMessage()).ifPresent(channel -> {
							this.plugin.getAdminChatManager().sendMessage(channel, player, event.getMessage().substring(channel.getPrefix().length()));
							event.setCancelled(true);
					}));
			
		}
	}
	

}
