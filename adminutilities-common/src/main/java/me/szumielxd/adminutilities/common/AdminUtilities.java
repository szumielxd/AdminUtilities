package me.szumielxd.adminutilities.common;

import java.util.function.Predicate;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.adminutilities.common.managers.AdminChatManager;
import me.szumielxd.adminutilities.common.managers.ChatReportManager;
import me.szumielxd.adminutilities.common.managers.ReportManager;
import me.szumielxd.adminutilities.common.objects.CommonProxy;
import me.szumielxd.adminutilities.common.objects.CommonSender;

public interface AdminUtilities {
	
	
	public @NotNull Predicate<CommonSender> hasPermAndNotLobby(@NotNull String permission);
	
	public @NotNull ReportManager getReportManager();
	
	public @NotNull ChatReportManager getChatReportManager();
	
	public @NotNull AdminChatManager getAdminChatManager();
	
	
	/*public boolean isLobby(ServerInfo server) {
		try {
			Class.forName("me.szumielxd.lobbysystem.LobbySystem");
			return LobbySystem.getInstance().isLobby(server);
		} catch (ClassNotFoundException e) {}
		return false;
	}*/
	
	public @NotNull Logger getLogger();
	
	
	public @NotNull CommonProxy getProxyServer();
	
	public @NotNull String getName();
	
	public @NotNull String getVersion();
	
	
	public void onEnable();
	
	public void onDisable();
	
	default public boolean isLobby(String server) {
		try {
			Class.forName("me.szumielxd.lobbysystem.LobbySystem");
			//return LobbySystem.getInstance().isLobby(server);
		} catch (ClassNotFoundException e) {}
		return false;
	}
	

}
