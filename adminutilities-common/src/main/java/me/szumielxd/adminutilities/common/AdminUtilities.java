package me.szumielxd.adminutilities.common;

import java.nio.file.Path;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.adminutilities.common.managers.AdminChatManager;
import me.szumielxd.adminutilities.common.managers.ChatReportManager;
import me.szumielxd.adminutilities.common.managers.ReportManager;
import me.szumielxd.adminutilities.common.objects.CommonProxy;

public interface AdminUtilities {
	
	public @NotNull ReportManager getReportManager();
	
	public @NotNull ChatReportManager getChatReportManager();
	
	public @NotNull AdminChatManager getAdminChatManager();
	
	public @NotNull Logger getLogger();
	
	
	public @NotNull CommonProxy getProxyServer();
	
	public @NotNull String getName();
	
	public @NotNull Path getDataDirectory();
	
	public @NotNull String getVersion();
	
	
	public void onEnable();
	
	public void onDisable();
	

}
