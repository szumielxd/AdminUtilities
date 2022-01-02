package me.szumielxd.adminutilities.common.commands;

import java.util.Arrays;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.data.ChatPlayer;
import me.szumielxd.adminutilities.common.data.OtherReport;
import me.szumielxd.adminutilities.common.objects.CommonPlayer;
import me.szumielxd.adminutilities.common.objects.CommonSender;
import me.szumielxd.adminutilities.common.utils.MiscUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ReportCommand extends CommonCommand {
	
	
	private final @NotNull Component PREFIX;
	private final @NotNull Component COMMAND_ERROR;
	private final @NotNull Component USAGE;
	private final @NotNull Component REPORTED;
	private final @NotNull Component ALREADY;
	private final @NotNull Component INVALID;
	private final @NotNull AdminUtilities plugin;
	

	public ReportCommand(@NotNull AdminUtilities plugin) {
		super(Config.COMMAND_REPORT_NAME.getString(), "adminutilities.command.report", Config.COMMAND_REPORT_ALIASES.getStringList().toArray(new String[0]));
		this.PREFIX = MiscUtil.parseComponent(Config.COMMAND_REPORT_PREFIX.getString(), false);
		this.COMMAND_ERROR = MiscUtil.parseComponent(Config.MESSAGES_COMMAND_ERROR.getString(), false);
		this.USAGE = MiscUtil.parseComponent(Config.COMMAND_REPORT_USAGE.getString(), false);
		this.REPORTED = MiscUtil.parseComponent(Config.COMMAND_REPORT_FORMAT_REPORTED.getString(), false);
		this.ALREADY = MiscUtil.parseComponent(Config.COMMAND_REPORT_FORMAT_ALREADY.getString(), false);
		this.INVALID = MiscUtil.parseComponent(Config.COMMAND_REPORT_FORMAT_ADMIN_INVALID.getString(), false);
		this.plugin = plugin;
	}

	@Override
	public void execute(@NotNull CommonSender s, String[] args) {
		
		if(!(s instanceof CommonPlayer)) {
			s.sendMessage(LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString()+Config.MESSAGES_CONSOLE_ERROR.getString()));
			return;
		}
		
		if(args.length < 2) {
			s.sendMessage(this.PREFIX.append(this.USAGE));
			return;
		}
		if (args[0].equalsIgnoreCase("-r") && s.hasPermission("adminutilities.command.report.resolve")) {
			if (args.length > 2) {
				if (args[2].equalsIgnoreCase("take")) {
					take(s, args[1]);
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
		if (s.hasPermission("adminutilities.command.report.format")) reason = MiscUtil.translateAlternateColorCodes('&', reason);
		OtherReport cr = new OtherReport(cp.getName(), s.getName(), reason);
		Component reasonComponent = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(cr.getReason());
		Function<Component,Component> rep = (comp) -> {
			comp = MiscUtil.deepReplace(comp, "accused", cr.getName());
			comp = MiscUtil.deepReplace(comp, "reporter", cr.getReporter());
			comp = MiscUtil.deepReplace(comp, "reason", LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().serialize(reasonComponent));
			return comp.replaceText(TextReplacementConfig.builder().matchLiteral("{accused}").replacement(cr.getName()).build())
					.replaceText(TextReplacementConfig.builder().matchLiteral("{reporter}").replacement(cr.getReporter()).build())
					.replaceText(TextReplacementConfig.builder().matchLiteral("{reason}").replacement(reasonComponent).build());
		};
		if(this.plugin.getReportManager().registerReport(cr)) {
			s.sendMessage(rep.apply(this.PREFIX.append(this.REPORTED)));
		} else {
			s.sendMessage(rep.apply(this.PREFIX.append(this.ALREADY)));
		}
		
	}
	
	private void take(CommonSender s, String code) {
		if(!this.plugin.getReportManager().getActiveReports().containsKey(code)) {
			s.sendMessage(this.PREFIX.append(this.INVALID));
			return;
		}
		this.plugin.getReportManager().unregisterReport(code, MiscUtil.getName(s));
	}

}
