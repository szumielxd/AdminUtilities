package me.szumielxd.adminutilities.velocity.objects;

import java.util.Arrays;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import me.szumielxd.adminutilities.common.objects.CommonSender;
import me.szumielxd.adminutilities.velocity.AdminUtilitiesVelocity;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocitySender implements CommonSender {
	
	
	protected final @NotNull AdminUtilitiesVelocity plugin;
	private final @NotNull CommandSource sender;
	
	
	public VelocitySender(@NotNull AdminUtilitiesVelocity plugin, @NotNull CommandSource sender) {
		this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
		this.sender = Objects.requireNonNull(sender, "sender cannot be null");
	}
	

	@Override
	public void sendMessage(@NotNull String message) {
		this.sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
	}

	@Override
	public void sendMessage(@NotNull Component message) {
		this.sender.sendMessage(message);
		
	}

	@Override
	public void sendMessage(@NotNull Component... message) {
		this.sender.sendMessage(Component.empty().children(Arrays.asList(message)));
	}
	
	@Override
	public void sendMessage(@NotNull Identity source, @NotNull Component message) {
		this.sender.sendMessage(source, message);
	}
	
	@Override
	public void sendMessage(@NotNull Identity source, @NotNull Component... message) {
		this.sender.sendMessage(source, Component.empty().children(Arrays.asList(message)));
	}
	
	@Override
	public boolean hasPermission(@NotNull String permission) {
		return this.sender.hasPermission(permission);
	}

	@Override
	public @NotNull String getName() {
		return "Console";
	}
	
	@Override
	public @NotNull String getDisplayName() {
		return this.getName();
	}

	@Override
	public void executeProxyCommand(@NotNull String command) {
		this.plugin.getProxy().getCommandManager().executeAsync(this.sender, command);
	}
	
	
	public static @NotNull VelocitySender wrap(@NotNull AdminUtilitiesVelocity plugin, @NotNull CommandSource sender) {
		if (Objects.requireNonNull(sender, "sender cannot be null") instanceof Player) return new VelocityPlayer(plugin, (Player) sender);
		return new VelocitySender(plugin, sender);
	}
	

}
