package me.szumielxd.adminutilities.common.commands;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.managers.AdminChatManager.ChannelType;
import me.szumielxd.adminutilities.common.objects.CommonSender;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AdminChatCommand extends CommonCommand {
	
	private final @NotNull AdminUtilities plugin;

	public AdminChatCommand(@NotNull AdminUtilities plugin) {
		super(Config.COMMAND_ADMINCHAT_NAME.getString(), ChannelType.ADMIN.getPermission(), Config.COMMAND_ADMINCHAT_ALIASES.getStringList().toArray(new String[0]));
		this.plugin = plugin;
	}

	@Override
	public void execute(@NotNull CommonSender sender, @NotNull String[] args) {
		if (args.length > 0) {
			this.plugin.getAdminChatManager().sendMessage(ChannelType.ADMIN, sender, String.join(" ", args));
			return;
		}
		sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString()+Config.COMMAND_ADMINCHAT_USAGE.getString()));
	}

}
