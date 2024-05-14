package me.szumielxd.adminutilities.common.commands;

import java.util.Arrays;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.data.ChatPlayer;
import me.szumielxd.adminutilities.common.data.ChatReport;
import me.szumielxd.adminutilities.common.objects.CommonPlayer;
import me.szumielxd.adminutilities.common.objects.CommonSender;
import me.szumielxd.adminutilities.common.utils.MiscUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatReportCommand extends CommonCommand {
	
	
	private final Component PREFIX;
	private final Component COMMAND_ERROR;
	private final Component USAGE;
	private final Component REPORTED;
	private final Component ALREADY;
	private final Component INVALID;
	private final AdminUtilities plugin;
	

	public ChatReportCommand(AdminUtilities plugin) {
		super(Config.COMMAND_CHATREPORT_NAME.getString(), "adminutilities.command.chatreport", Config.COMMAND_CHATREPORT_ALIASES.getStringList().toArray(new String[0]));
		this.PREFIX = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_PREFIX.getString(), false);
		this.COMMAND_ERROR = MiscUtil.parseComponent(Config.MESSAGES_COMMAND_ERROR.getString(), false);
		this.USAGE = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_USAGE.getString(), false);
		this.REPORTED = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_FORMAT_REPORTED.getString(), false);
		this.ALREADY = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_FORMAT_ALREADY.getString(), false);
		this.INVALID = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_FORMAT_ADMIN_INVALID.getString(), false);
		this.plugin = plugin;
	}

	@Override
	public void execute(@NotNull CommonSender s, @NotNull String[] args) {
		
		if(!(s instanceof CommonPlayer)) {
			s.sendMessage(LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString() + Config.MESSAGES_CONSOLE_ERROR.getString()));
			return;
		}
		
		if(args.length < 2) {
			s.sendMessage(this.PREFIX.append(this.USAGE));
			return;
		}
		if (args[0].equalsIgnoreCase("-r") && s.hasPermission("adminutilities.command.chatreport.resolve")) {
			if (args.length > 2) {
				if (args[2].equalsIgnoreCase("accept")) {
					accept(s, args[1]);
					return;
				}
				if (args[2].equalsIgnoreCase("reject")) {
					reject(s, args[1]);
					return;
				}
			}
		}
		CommonPlayer accused = this.plugin.getProxyServer().getPlayer(args[0]);
		if(accused == null) {
			s.sendMessage(this.PREFIX.append(this.USAGE));
			return;
		}
		ChatPlayer cp = ChatPlayer.getChatPlayer(accused.getUniqueId());
		if(cp == null) {
			s.sendMessage(this.PREFIX.append(this.COMMAND_ERROR));
			return;
		}
		String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		if (s.hasPermission("adminutilities.command.chatreport.format")) reason = MiscUtil.translateAlternateColorCodes('&', reason);
		ChatReport cr = new ChatReport(cp, s.getName(), reason);
		Component reasonComponent = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(cr.getReason());
		UnaryOperator<Component> rep = comp -> {
			comp = MiscUtil.deepReplace(comp, "accused", cr.getName());
			comp = MiscUtil.deepReplace(comp, "reporter", cr.getReporter());
			comp = MiscUtil.deepReplace(comp, "reason", LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().serialize(reasonComponent));
			return comp.replaceText(TextReplacementConfig.builder().matchLiteral("{accused}").replacement(cr.getName()).build())
					.replaceText(TextReplacementConfig.builder().matchLiteral("{reporter}").replacement(cr.getReporter()).build())
					.replaceText(TextReplacementConfig.builder().matchLiteral("{reason}").replacement(reasonComponent).build());
		};
		if(this.plugin.getChatReportManager().registerReport(cr)) {
			s.sendMessage(rep.apply(this.PREFIX.append(this.REPORTED)));
		} else {
			s.sendMessage(rep.apply(this.PREFIX.append(this.ALREADY)));
		}
		
	}
	
	private void accept(CommonSender s, String code) {
		if(!this.plugin.getChatReportManager().getActiveReports().containsKey(code)) {
			s.sendMessage(this.PREFIX.append(this.INVALID));
			return;
		}
		this.plugin.getChatReportManager().unregisterReport(code, MiscUtil.getName(s), true);
	}
	
	private void reject(CommonSender s, String code) {
		if(!this.plugin.getChatReportManager().getActiveReports().containsKey(code)) {
			s.sendMessage(this.PREFIX.append(this.INVALID));
			return;
		}
		this.plugin.getChatReportManager().unregisterReport(code, MiscUtil.getName(s), false);
	}

}
