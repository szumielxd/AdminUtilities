package me.szumielxd.adminutilities.velocity.objects;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.szumielxd.adminutilities.common.objects.CommonPlayer;
import me.szumielxd.adminutilities.common.objects.CommonProxy;
import me.szumielxd.adminutilities.common.objects.CommonScheduler;
import me.szumielxd.adminutilities.velocity.AdminUtilitiesVelocity;

public class VelocityProxy implements CommonProxy {
	
	
	private final @NotNull AdminUtilitiesVelocity plugin;
	private final @NotNull VelocityScheduler scheduler;
	
	
	public VelocityProxy(@NotNull AdminUtilitiesVelocity plugin) {
		this.plugin = plugin;
		this.scheduler = new VelocityScheduler(plugin);
	}
	

	@Override
	public @Nullable CommonPlayer getPlayer(@NotNull UUID uuid) {
		return this.plugin.getProxy().getPlayer(uuid).map(p -> new VelocityPlayer(this.plugin, p)).orElse(null);
	}

	@Override
	public @Nullable CommonPlayer getPlayer(@NotNull String name) {
		return this.plugin.getProxy().getPlayer(name).map(p -> new VelocityPlayer(this.plugin, p)).orElse(null);
	}

	@Override
	public @NotNull Collection<CommonPlayer> getPlayers() {
		return this.plugin.getProxy().getAllPlayers().parallelStream().map(p -> new VelocityPlayer(this.plugin, p)).collect(Collectors.toList());
	}

	@Override
	public @NotNull Collection<CommonPlayer> getPlayers(@NotNull String serverName) {
		return this.plugin.getProxy().getServer(Objects.requireNonNull(serverName, "serverName cannot be null"))
				.map(RegisteredServer::getPlayersConnected).orElse(Collections.emptyList())
				.parallelStream().map(p -> new VelocityPlayer(this.plugin, p)).collect(Collectors.toList());
	}
	
	@Override
	public @NotNull CommonScheduler getScheduler() {
		return this.scheduler;
	}
	

}
