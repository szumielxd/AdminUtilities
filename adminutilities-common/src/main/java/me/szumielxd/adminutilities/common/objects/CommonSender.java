package me.szumielxd.adminutilities.common.objects;

import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

public interface CommonSender {
	
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public void sendMessage(@NotNull String message);
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public void sendMessage(@NotNull Component message);
	
	/**
	 * Send message to this sender.
	 * 
	 * @param message message to send
	 */
	public void sendMessage(@NotNull Component... message);
	
	/**
	 * Send message to this sender.
	 * 
	 * @param identity identity of sender
	 * @param message message to send
	 */
	public void sendMessage(@NotNull Identity source, @NotNull Component message);
	
	/**
	 * Send message to this sender.
	 * 
	 * @param identity identity of sender
	 * @param message message to send
	 */
	public void sendMessage(@NotNull Identity server, @NotNull Component... message);
	
	/**
	 * Checks if this user has the specified permission node.
	 * 
	 * @param permission the node to check
	 * @return true if he has this node
	 */
	public boolean hasPermission(@NotNull String permission);
	
	/**
	 * Get name of this sender.
	 * 
	 * @return name of sender
	 */
	public @NotNull String getName();
	
	/**
	 * Get custom display name if set, otherwise plain name.
	 * 
	 * @return sender's display name
	 */
	public @NotNull String getDisplayName();
	
	/**
	 * Make this sender run command.
	 * 
	 * @param command command to execute
	 */
	public void executeProxyCommand(@NotNull String command);
	

}
