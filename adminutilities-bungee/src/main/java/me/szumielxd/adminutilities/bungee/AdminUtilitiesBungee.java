package me.szumielxd.adminutilities.bungee;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.adminutilities.bungee.commands.BungeeCommandWrapper;
import me.szumielxd.adminutilities.bungee.listeners.ChatListener;
import me.szumielxd.adminutilities.bungee.objects.BungeeProxy;
import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.AdminUtilitiesProvider;
import me.szumielxd.adminutilities.common.commands.AdminChatCommand;
import me.szumielxd.adminutilities.common.commands.ChatReportCommand;
import me.szumielxd.adminutilities.common.commands.CommonCommand;
import me.szumielxd.adminutilities.common.commands.HelpopCommand;
import me.szumielxd.adminutilities.common.commands.MainCommand;
import me.szumielxd.adminutilities.common.commands.ModChatCommand;
import me.szumielxd.adminutilities.common.commands.ReportCommand;
import me.szumielxd.adminutilities.common.commands.StafflistCommand;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.data.ChatPlayer;
import me.szumielxd.adminutilities.common.managers.AdminChatManager;
import me.szumielxd.adminutilities.common.managers.ChatReportManager;
import me.szumielxd.adminutilities.common.managers.ReportManager;
import me.szumielxd.adminutilities.common.objects.CommonProxy;
import me.szumielxd.adminutilities.common.objects.CommonSender;
import me.szumielxd.lobbysystem.LobbySystem;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class AdminUtilitiesBungee extends Plugin implements AdminUtilities {
	

	private BungeeProxy proxy;
	
	private BungeeAudiences adventure = null;
	private ReportManager reportManager;
	private ChatReportManager chatReportManager;
	private AdminChatManager adminChatManager;
	
	
	public Predicate<CommonSender> hasPermAndNotLobby(String permission) {
		return (sender) -> {
			if (!sender.hasPermission(permission)) return false;
			if (sender instanceof ProxiedPlayer) {
				ProxiedPlayer player = (ProxiedPlayer) sender;
				if (player.getServer() == null) return false;
				return !this.isLobby(player.getServer().getInfo());
			}
			return true;
		};
	}
	
	
	public BungeeAudiences adventure() {
		if (this.adventure == null) throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
		return this.adventure;
	}
	
	public ReportManager getReportManager() {
		return this.reportManager;
	}
	public ChatReportManager getChatReportManager() {
		return this.chatReportManager;
	}
	public AdminChatManager getAdminChatManager() {
		return this.adminChatManager;
	}
	
	public boolean isLobby(ServerInfo server) {
		try {
			Class.forName("me.szumielxd.lobbysystem.LobbySystem");
			return LobbySystem.getInstance().isLobby(server);
		} catch (ClassNotFoundException e) {}
		return false;
	}
	
	
	@Override
	public void onEnable() {
		AdminUtilitiesProvider.init(this);
		this.proxy = new BungeeProxy(this);
		this.adventure = BungeeAudiences.create(this);
		Config.load(new File(this.getDataFolder(), "config.yml"), this);
		this.registerCommand(new MainCommand(this));
		this.getProxy().getPluginManager().registerListener(this, new ChatListener(this));
		this.getLogger().info("Loading AdminChannels...");
		this.adminChatManager = new AdminChatManager(this);
		this.registerCommand(new AdminChatCommand(this));
		this.registerCommand(new ModChatCommand(this));
		this.getLogger().info("Loading Report...");
		this.reportManager = new ReportManager(this);
		this.registerCommand(new ReportCommand(this));
		this.getLogger().info("Loading ChatReport...");
		this.chatReportManager = new ChatReportManager(this);
		this.registerCommand(new ChatReportCommand(this));
		ChatPlayer.load();
		this.getLogger().info("Loading HelpOP...");
		this.registerCommand(new HelpopCommand(this));
		this.getLogger().info("Loading Stafflist...");
		this.registerCommand(new StafflistCommand(this));
		this.getLogger().info("Successfully loaded AdminUtilities!");
	}
	
	
	private void registerCommand(@NotNull CommonCommand command) {
		this.getProxy().getPluginManager().registerCommand(this, new BungeeCommandWrapper(this, command));
	}
	
	
	@Override
	public void onDisable() {
		this.getLogger().info("Disabling all announcements...");
		this.getProxy().getScheduler().cancel(this);
		if (this.adventure != null) {
			this.adventure.close();
			this.adventure = null;
		}
		try {
			Class<?> BungeeAudiencesImpl = Class.forName("net.kyori.adventure.platform.bungeecord.BungeeAudiencesImpl");
			Field f = BungeeAudiencesImpl.getDeclaredField("INSTANCES");
			f.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, BungeeAudiences> INSTANCES = (Map<String, BungeeAudiences>) f.get(null);
			INSTANCES.remove(this.getDescription().getName());
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		this.getProxy().getPluginManager().unregisterListeners(this);
		this.getProxy().getPluginManager().unregisterCommands(this);
		this.getLogger().info("Well done. Time to sleep!");
	}


	@Override
	public @NotNull CommonProxy getProxyServer() {
		return this.proxy;
	}


	@Override
	public @NotNull String getName() {
		return this.getDescription().getName();
	}


	@Override
	public @NotNull String getVersion() {
		return this.getDescription().getVersion();
	}
	

}
