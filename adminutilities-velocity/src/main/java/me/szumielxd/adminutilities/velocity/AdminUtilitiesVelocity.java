package me.szumielxd.adminutilities.velocity;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

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
import me.szumielxd.adminutilities.common.objects.CommonPlayer;
import me.szumielxd.adminutilities.common.objects.CommonProxy;
import me.szumielxd.adminutilities.common.objects.CommonSender;
import me.szumielxd.adminutilities.velocity.commands.VelocityCommandWrapper;
import me.szumielxd.adminutilities.velocity.listeners.ChatListener;
import me.szumielxd.adminutilities.velocity.objects.VelocityProxy;

@Plugin(
		id = "${parent.artifactId}",
		name = "${pluginName}",
		version = "${version}",
		authors = { "${author}" },
		description = "${description}",
		url = "${parent.url}"
)
public class AdminUtilitiesVelocity implements AdminUtilities {
	
	
	private final ProxyServer server;
	private final Logger logger;
	private final File dataFolder;
	
	private VelocityProxy proxy;
	
	private ReportManager reportManager;
	private ChatReportManager chatReportManager;
	private AdminChatManager adminChatManager;
	
	
	
	@Inject
	public AdminUtilitiesVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
		this.server = server;
		this.logger = logger;
		this.dataFolder = dataDirectory.toFile();
	}
	
	
	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
	    this.onEnable();
	}
	
	
	@Override
	public Predicate<CommonSender> hasPermAndNotLobby(String permission) {
		return (sender) -> {
			if (!sender.hasPermission(permission)) return false;
			if (sender instanceof CommonPlayer) {
				CommonPlayer player = (CommonPlayer) sender;
				if (player.getWorldName() == null) return false;
				return !this.isLobby(player.getWorldName());
			}
			return true;
		};
	}
	
	
	@Override
	public @NotNull Logger getLogger() {
		return this.logger;
	}
	
	
	public @NotNull CommonProxy getProxyServer() {
		return this.proxy;
	}
	
	
	public @NotNull ProxyServer getProxy() {
		return this.server;
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
	
	
	@Override
	public void onEnable() {
		AdminUtilitiesProvider.init(this);
		this.proxy = new VelocityProxy(this);
		
		Config.load(new File(this.dataFolder, "config.yml"), this);
		this.registerCommand(new MainCommand(this));
		this.getProxy().getEventManager().register(this, new ChatListener(this));
		
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
		CommandManager mgr = this.getProxy().getCommandManager();
		CommandMeta meta = mgr.metaBuilder(command.getName()).aliases(command.getAliases()).build();
		mgr.register(meta, new VelocityCommandWrapper(this, command));
	}
	
	
	@Override
	public void onDisable() {
		this.getLogger().info("Disabling all announcements...");
		this.getProxyServer().getScheduler().cancelAll();
		//this.getProxy().getPluginManager().unregisterListeners(this);
		//this.getProxy().getCommandManager()..unregisterCommands(this);
		this.getLogger().info("Well done. Time to sleep!");
	}


	@Override
	public @NotNull String getName() {
		return this.getProxy().getPluginManager().ensurePluginContainer(this).getDescription().getName().orElse("");
	}


	@Override
	public @NotNull String getVersion() {
		return this.getProxy().getPluginManager().ensurePluginContainer(this).getDescription().getVersion().orElse("");
	}
	

}
