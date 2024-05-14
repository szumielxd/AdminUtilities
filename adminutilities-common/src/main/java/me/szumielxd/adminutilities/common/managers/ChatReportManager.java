package me.szumielxd.adminutilities.common.managers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.data.ChatReport;
import me.szumielxd.adminutilities.common.objects.CommonPlayer;
import me.szumielxd.adminutilities.common.objects.CommonScheduler.ExecutedTask;
import me.szumielxd.adminutilities.common.utils.MiscUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatReportManager {
	
	
	private Map<String, Entry<ChatReport, ExecutedTask>> reports = new HashMap<>();
	public Map<String, Entry<ChatReport, ExecutedTask>> getActiveReports(){
		return new HashMap<>(reports);
	}
	
	
	private enum Replacer {
		
		ACCUSED("accused"),
		ACCUSED_DISPLAY("accusedDisplay"),
		REPORTER("reporter"),
		REPORTER_DISPLAY("reporterDisplay"),
		ADMIN("admin"),
		ADMIN_DISPLAY("adminDisplay"),
		DATE("date"),
		TIME("time"),
		REASON("reason"),
		ID("id"),
		MESSAGE("message"),
		MESSAGES_LIST("messages");
		
		private final String match;
		private Replacer(String match) {
			this.match = match;
		}
		public String getMatch() {
			return this.match;
		}
		public TextReplacementConfig rep(Object toReplace) {
			if (toReplace instanceof ComponentLike rep) return TextReplacementConfig.builder().matchLiteral("{"+match+"}").replacement(rep).build();
			return TextReplacementConfig.builder().matchLiteral("{"+match+"}").replacement(String.valueOf(toReplace)).build();
		}
		
		
	}
	
	private enum ReportResult {
		TIMEOUT,
		ACCEPT,
		REJECT
	}
	
	
	private final Component PREFIX;
	private final Component TIMEOUT;
	private final Component ACCEPTED;
	private final Component REJECTED;
	private final Component ADMIN_REPORTED;
	private final Component ADMIN_TIMEOUT;
	private final Component ADMIN_ACCEPTED;
	private final Component ADMIN_REJECTED;
	private final Component MESSAGES_LINE;
	
	
	private final AdminUtilities plugin;
	
	
	public ChatReportManager(AdminUtilities plugin) {
		this.plugin = plugin;
		this.PREFIX = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_PREFIX.getString(), false);
		this.TIMEOUT = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_FORMAT_TIMEOUT.getString(), true);
		this.ACCEPTED = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_FORMAT_ACCEPTED.getString(), true);
		this.REJECTED = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_FORMAT_REJECTED.getString(), true);
		this.ADMIN_REPORTED = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_FORMAT_ADMIN_REPORTED.getString(), true);
		this.ADMIN_TIMEOUT = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_FORMAT_ADMIN_TIMEOUT.getString(), true);
		this.ADMIN_ACCEPTED = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_FORMAT_ADMIN_ACCEPTED.getString(), true);
		this.ADMIN_REJECTED = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_FORMAT_ADMIN_REJECTED.getString(), true);
		this.MESSAGES_LINE = MiscUtil.parseComponent(Config.COMMAND_CHATREPORT_FORMAT_MESSAGES_LINE.getString(), true);
	}
	
	
	private @NotNull Component buildMessagesComponent(@NotNull List<Entry<Long, String>> messages) {
		if (messages.isEmpty()) return Component.empty();
		Component component = Component.empty();
		List<Component> list = new ArrayList<>();
		for (Entry<Long, String> e : messages) {
			list.add(this.MESSAGES_LINE
					.replaceText(Replacer.DATE.rep(MiscUtil.parseOnlyDate(e.getKey())))
					.replaceText(Replacer.TIME.rep(MiscUtil.parseOnlyTime(e.getKey())))
					.replaceText(Replacer.MESSAGE.rep(e.getValue())));
			list.add(Component.newline());
		}
		list.remove(list.size()-1);
		return component.children(list);
	}
	
	
	
	public boolean registerReport(@NotNull ChatReport report) {
		
		List<String> keys = new ArrayList<>(reports.keySet());
		Map<String, Entry<ChatReport, ExecutedTask>> map = new HashMap<>(reports);
		if(!keys.isEmpty()) for(String code : keys) {
			ChatReport cr = map.get(code).getKey();
			if(cr.getMessages().equals(report.getMessages())) return false;
		}
		String reportCode;
		do {
			reportCode = MiscUtil.randomString(7);
		} while (reports.containsKey(reportCode));
		final String code = reportCode;
		ExecutedTask task = this.plugin.getProxyServer().getScheduler().runTaskLater(() -> {
			Component reason = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(report.getReason());
			reports.remove(code);
			trySendTimeoutReportToAdmins(report, reason);
			trySendTimeoutReportToPlayer(report, reason);
			this.logReport(report, null, ReportResult.TIMEOUT);
		}, Config.COMMAND_CHATREPORT_TIMEOUT.getInt(), TimeUnit.SECONDS);
		reports.put(code, new SimpleEntry<>(report, task));
		sendNewReport(code);
		return true;
		
	}
	
	
	public boolean unregisterReport(@NotNull String code, @NotNull String admin, @NotNull boolean accepted) {
		var reportEntry = reports.get(code);
		if(reportEntry != null) {
			ChatReport report = reportEntry.getKey();
			ExecutedTask task = reportEntry.getValue();
			if (task != null) {
				task.cancel();
				reports.remove(code);
				Component messages = buildMessagesComponent(report.getMessages());
				Component reason = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(report.getReason());
				trySendUnregisterReportToAdmins(report, code, messages, reason, admin, accepted);
				trySendUnregisterReportToPlayer(report, code, messages, reason, admin, accepted);
				this.logReport(report, admin, accepted ? ReportResult.ACCEPT : ReportResult.REJECT);
				return true;
			}
		}
		return false;
	}
	
	private void trySendUnregisterReportToAdmins(@NotNull ChatReport report, @NotNull String code, @NotNull Component messages, @NotNull Component reason, @NotNull String admin, @NotNull boolean accepted) {
		Component admmsg = accepted ? this.ADMIN_ACCEPTED : this.ADMIN_REJECTED;
		Component comp = admmsg.replaceText(Replacer.ACCUSED.rep(report.getName())).replaceText(Replacer.REPORTER.rep(report.getReporter()))
				.replaceText(Replacer.ADMIN.rep(admin)).replaceText(Replacer.ID.rep(code))
				.replaceText(Replacer.DATE.rep(MiscUtil.parseOnlyDate(report.getTimestamp())))
				.replaceText(Replacer.TIME.rep(MiscUtil.parseOnlyTime(report.getTimestamp())))
				.replaceText(Replacer.MESSAGES_LIST.rep(messages))
				.replaceText(Replacer.REASON.rep(reason));
		comp = MiscUtil.deepReplace(comp, Replacer.ACCUSED.getMatch(), report.getName());
		comp = MiscUtil.deepReplace(comp, Replacer.REPORTER.getMatch(), report.getReporter());
		comp = MiscUtil.deepReplace(comp, Replacer.ID.getMatch(), code);
		comp = MiscUtil.deepReplace(comp, Replacer.ADMIN.getMatch(), admin);
		comp = MiscUtil.deepReplace(comp, Replacer.DATE.getMatch(), MiscUtil.parseOnlyDate(report.getTimestamp()));
		comp = MiscUtil.deepReplace(comp, Replacer.TIME.getMatch(), MiscUtil.parseOnlyTime(report.getTimestamp()));
		comp = MiscUtil.deepReplace(comp, Replacer.MESSAGES_LIST.getMatch(), messages);
		comp = MiscUtil.deepReplace(comp, Replacer.REASON.getMatch(), reason);
		final Component c = comp;
		this.plugin.getProxyServer().getPlayers().stream()
				.filter(this::canSeeChatReport)
				.forEach(p -> p.sendMessage(this.PREFIX.append(c)));
	}
	
	private void trySendUnregisterReportToPlayer(@NotNull ChatReport report, @NotNull String code, @NotNull Component messages, @NotNull Component reason, @NotNull String admin, @NotNull boolean accepted) {
		CommonPlayer player = this.plugin.getProxyServer().getPlayer(report.getReporter());
		if (player != null) {
			Component message = accepted ? this.ACCEPTED : this.REJECTED;
			Component comp = message.replaceText(Replacer.ACCUSED.rep(report.getName())).replaceText(Replacer.REPORTER.rep(report.getReporter()))
					.replaceText(Replacer.ADMIN.rep(admin)).replaceText(Replacer.ID.rep(code))
					.replaceText(Replacer.DATE.rep(MiscUtil.parseOnlyDate(report.getTimestamp())))
					.replaceText(Replacer.TIME.rep(MiscUtil.parseOnlyTime(report.getTimestamp())))
					.replaceText(Replacer.MESSAGES_LIST.rep(messages))
					.replaceText(Replacer.REASON.rep(reason));
			comp = MiscUtil.deepReplace(comp, Replacer.ACCUSED.getMatch(), report.getName());
			comp = MiscUtil.deepReplace(comp, Replacer.REPORTER.getMatch(), report.getReporter());
			comp = MiscUtil.deepReplace(comp, Replacer.ID.getMatch(), code);
			comp = MiscUtil.deepReplace(comp, Replacer.ADMIN.getMatch(), admin);
			comp = MiscUtil.deepReplace(comp, Replacer.DATE.getMatch(), MiscUtil.parseOnlyDate(report.getTimestamp()));
			comp = MiscUtil.deepReplace(comp, Replacer.TIME.getMatch(), MiscUtil.parseOnlyTime(report.getTimestamp()));
			comp = MiscUtil.deepReplace(comp, Replacer.MESSAGES_LIST.getMatch(), messages);
			comp = MiscUtil.deepReplace(comp, Replacer.REASON.getMatch(), reason);
			player.sendMessage(this.PREFIX.append(comp));
		}
	}
	
	private void trySendTimeoutReportToAdmins(@NotNull ChatReport report, @NotNull Component reason) {
		if(ADMIN_TIMEOUT != null) {
			Component comp = ADMIN_TIMEOUT.replaceText(Replacer.ACCUSED.rep(report.getName())).replaceText(Replacer.REPORTER.rep(report.getReporter())).replaceText(Replacer.REASON.rep(reason));
			comp = MiscUtil.deepReplace(comp, Replacer.ACCUSED.getMatch(), report.getName());
			comp = MiscUtil.deepReplace(comp, Replacer.REPORTER.getMatch(), report.getReporter());
			comp = MiscUtil.deepReplace(comp, Replacer.REASON.getMatch(), reason);
			final Component c = comp;
			plugin.getProxyServer().getPlayers().stream()
					.filter(this::canSeeChatReport)
					.forEach(p -> p.sendMessage(PREFIX.append(c)));
		}
	}
	
	private void trySendTimeoutReportToPlayer(@NotNull ChatReport report, @NotNull Component reason) {
		if (TIMEOUT != null) {
			CommonPlayer player = plugin.getProxyServer().getPlayer(report.getReporter());
			if (player != null) {
				Component comp = TIMEOUT.replaceText(Replacer.ACCUSED.rep(report.getName())).replaceText(Replacer.REPORTER.rep(report.getReporter())).replaceText(Replacer.REASON.rep(reason));
				comp = MiscUtil.deepReplace(comp, Replacer.ACCUSED.getMatch(), report.getName());
				comp = MiscUtil.deepReplace(comp, Replacer.REPORTER.getMatch(), report.getReporter());
				comp = MiscUtil.deepReplace(comp, Replacer.REASON.getMatch(), reason);
				player.sendMessage(PREFIX.append(comp));
			}
		}
	}
	
	
	private void sendNewReport(@NotNull String code) {
		if (reports.containsKey(code)) {
			ChatReport report = reports.get(code).getKey();
			this.plugin.getProxyServer().getPlayers().stream()
					.filter(this::canSeeChatReport)
					.forEach(report.getAdmins()::add);
			if (this.ADMIN_REPORTED != null) {
				Component reason = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(report.getReason());
				Component messages = buildMessagesComponent(report.getMessages());
				Component comp = this.ADMIN_REPORTED.replaceText(Replacer.ACCUSED.rep(report.getName()))
						.replaceText(Replacer.REPORTER.rep(report.getReporter()))
						.replaceText(Replacer.ID.rep(code))
						.replaceText(Replacer.DATE.rep(MiscUtil.parseOnlyDate(report.getTimestamp())))
						.replaceText(Replacer.TIME.rep(MiscUtil.parseOnlyTime(report.getTimestamp())))
						.replaceText(Replacer.REASON.rep(reason))
						.replaceText(Replacer.MESSAGES_LIST.rep(messages));
				comp = MiscUtil.deepReplace(comp, Replacer.ACCUSED.getMatch(), report.getName());
				comp = MiscUtil.deepReplace(comp, Replacer.REPORTER.getMatch(), report.getReporter());
				comp = MiscUtil.deepReplace(comp, Replacer.ID.getMatch(), code);
				comp = MiscUtil.deepReplace(comp, Replacer.DATE.getMatch(), MiscUtil.parseOnlyDate(report.getTimestamp()));
				comp = MiscUtil.deepReplace(comp, Replacer.TIME.getMatch(), MiscUtil.parseOnlyTime(report.getTimestamp()));
				comp = MiscUtil.deepReplace(comp, Replacer.MESSAGES_LIST.getMatch(), messages);
				comp = MiscUtil.deepReplace(comp, Replacer.REASON.getMatch(), reason);
				final Component c = comp;
				report.getAdmins().forEach(p -> p.sendMessage(this.PREFIX.append(c)));
			}
		}
		
	}
	
	private void logReport(@NotNull ChatReport report, @Nullable String admin, @NotNull ReportResult result) {
		String admins = report.getAdmins().stream()
				.map(a -> a.getName())
				.collect(Collectors.joining(","));
		String messages = report.getMessages().stream()
				.map(Entry::getValue)
				.map(s -> "  " + s)
				.collect(Collectors.joining(System.lineSeparator()));
		String str = """
				========== %s - %s ==========
				Reporter: %s
				Reason: %s
				Result: %s
				Admin: %s
				All admins: [%s]
				Chat:
				%s
				
				""".formatted(
						report.getName(),
						LocalDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
						report.getReporter(),
						report.getReason(),
						result,
						admin != null ? admin : "-",
						admins,
						messages);
		try {
			Path logsFolder = this.plugin.getDataDirectory().resolve("logs");
			if (!Files.isDirectory(logsFolder)) {
				if (Files.exists(logsFolder)) {
					Files.move(logsFolder, Path.of(logsFolder.toString() + "-backup"));
				}
				Files.createDirectory(logsFolder);
			}
			Files.writeString(logsFolder.resolve("chatreports.log"), str, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean canSeeChatReport(CommonPlayer player) {
		return player.hasPermission("adminutilities.admin.notify.chatreport");
	}
	

}
