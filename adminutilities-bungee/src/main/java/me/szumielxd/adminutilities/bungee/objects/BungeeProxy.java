package me.szumielxd.adminutilities.bungee.objects;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.adminutilities.bungee.AdminUtilitiesBungee;
import me.szumielxd.adminutilities.common.objects.CommonPlayer;
import me.szumielxd.adminutilities.common.objects.CommonProxy;
import me.szumielxd.adminutilities.common.objects.CommonScheduler;
import me.szumielxd.adminutilities.common.objects.CommonSender;
import net.md_5.bungee.api.config.ServerInfo;

public class BungeeProxy implements CommonProxy {


	private final @NotNull AdminUtilitiesBungee plugin;
	private final @NotNull BungeeScheduler scheduler;
	
	
	public BungeeProxy(@NotNull AdminUtilitiesBungee plugin) {
		this.plugin = plugin;
		this.scheduler = new BungeeScheduler(plugin);
	}
	

	@Override
	public @Nullable CommonPlayer getPlayer(@NotNull UUID uuid) {
		return Optional.ofNullable(this.plugin.getProxy().getPlayer(uuid)).map(p -> new BungeePlayer(this.plugin, p)).orElse(null);
	}

	@Override
	public @Nullable CommonPlayer getPlayer(@NotNull String name) {
		return Optional.ofNullable(this.plugin.getProxy().getPlayer(name)).map(p -> new BungeePlayer(this.plugin, p)).orElse(null);
	}

	@Override
	public @NotNull Collection<CommonPlayer> getPlayers() {
		return this.plugin.getProxy().getPlayers().parallelStream().map(p -> new BungeePlayer(this.plugin, p)).collect(Collectors.toList());
	}

	@Override
	public @NotNull Optional<Collection<CommonPlayer>> getPlayers(@NotNull String serverName) {
		return Optional.ofNullable(this.plugin.getProxy().getServerInfo(Objects.requireNonNull(serverName, "serverName cannot be null")))
				.map(ServerInfo::getPlayers).map(list -> list.parallelStream().map(p -> new BungeePlayer(this.plugin, p)).collect(Collectors.toList()));
	}
	
	@Override
	public @NotNull CommonSender getConsole() {
		return BungeeSender.wrap(this.plugin, this.plugin.getProxy().getConsole());
	}
	
	@Override
	public @NotNull CommonScheduler getScheduler() {
		return this.scheduler;
	}
	

}
