package me.szumielxd.adminutilities.velocity.commands;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.velocitypowered.api.command.SimpleCommand;

import me.szumielxd.adminutilities.common.commands.CommonCommand;
import me.szumielxd.adminutilities.velocity.AdminUtilitiesVelocity;
import me.szumielxd.adminutilities.velocity.objects.VelocitySender;

public class VelocityCommandWrapper implements SimpleCommand {
	
	
	private final @NotNull AdminUtilitiesVelocity plugin;
	private final @NotNull CommonCommand command;
	
	
	public VelocityCommandWrapper(@NotNull AdminUtilitiesVelocity plugin, @NotNull CommonCommand command) {
		this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
		this.command = Objects.requireNonNull(command, "command cannot be null");
	}
	

	@Override
	public void execute(@NotNull Invocation invocation) {
		this.command.execute(VelocitySender.wrap(this.plugin, invocation.source()), invocation.arguments());
	}
	
	
	@Override
	public List<String> suggest(@NotNull Invocation invocation) {
		return this.command.onTabComplete(VelocitySender.wrap(this.plugin, invocation.source()), invocation.arguments());
	}
	
	
	@Override
	public boolean hasPermission(@NotNull Invocation invocation) {
		return this.command.getPermission() == null || invocation.source().hasPermission(this.command.getPermission());
	}

}
