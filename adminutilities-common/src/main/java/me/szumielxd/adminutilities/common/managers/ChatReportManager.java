package me.szumielxd.adminutilities.common.managers;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

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
	
	
	private HashMap<String, Entry<ChatReport, ExecutedTask>> reports = new HashMap<>();
	public HashMap<String, Entry<ChatReport, ExecutedTask>> getActiveReports(){
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
			if (toReplace instanceof ComponentLike) return TextReplacementConfig.builder().matchLiteral("{"+match+"}").replacement((ComponentLike) toReplace).build();
			return TextReplacementConfig.builder().matchLiteral("{"+match+"}").replacement(String.valueOf(toReplace)).build();
		}
		
		
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
	
	
	private Component buildMessagesComponent(ArrayList<Entry<Long, String>> messages) {
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
	
	
	
	public boolean registerReport(final ChatReport report) {
		
		ArrayList<String> keys = new ArrayList<String>(reports.keySet());
		HashMap<String, Entry<ChatReport, ExecutedTask>> map = new HashMap<>(reports);
		if(!keys.isEmpty()) for(String code : keys) {
			ChatReport cr = map.get(code).getKey();
			if(cr.getMessages().equals(report.getMessages())) return false;
		}
		String reportCode;
		do {
			reportCode = MiscUtil.randomString(7);
		} while (reports.containsKey(reportCode));
		final String code = reportCode;
		ExecutedTask task = this.plugin.getProxyServer().getScheduler().runTaskLater(new Runnable() {
			@Override
			public void run() {
				Component reason = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(report.getReason());
				reports.remove(code);
				if(ADMIN_TIMEOUT != null) {
					Component comp = ADMIN_TIMEOUT.replaceText(Replacer.ACCUSED.rep(report.getName())).replaceText(Replacer.REPORTER.rep(report.getReporter())).replaceText(Replacer.REASON.rep(reason));
					comp = MiscUtil.deepReplace(comp, Replacer.ACCUSED.getMatch(), report.getName());
					comp = MiscUtil.deepReplace(comp, Replacer.REPORTER.getMatch(), report.getReporter());
					comp = MiscUtil.deepReplace(comp, Replacer.REASON.getMatch(), reason);
					final Component c = comp;
					plugin.getProxyServer().getPlayers().parallelStream().filter(plugin.hasPermAndNotLobby("adminutilities.admin.notify.chatreport")).forEach(p -> p.sendMessage(PREFIX.append(c)));
				}
				CommonPlayer pp = plugin.getProxyServer().getPlayer(report.getReporter());
				if (pp != null && TIMEOUT != null) {
					Component comp = TIMEOUT.replaceText(Replacer.ACCUSED.rep(report.getName())).replaceText(Replacer.REPORTER.rep(report.getReporter())).replaceText(Replacer.REASON.rep(reason));
					comp = MiscUtil.deepReplace(comp, Replacer.ACCUSED.getMatch(), report.getName());
					comp = MiscUtil.deepReplace(comp, Replacer.REPORTER.getMatch(), report.getReporter());
					comp = MiscUtil.deepReplace(comp, Replacer.REASON.getMatch(), reason);
					pp.sendMessage(PREFIX.append(comp));
				}
			}
		}, Config.COMMAND_CHATREPORT_TIMEOUT.getInt(), TimeUnit.SECONDS);
		reports.put(code, new SimpleEntry<>(report, task));
		sendNewReport(code);
		return true;
		
	}
	
	
	public boolean unregisterReport(final String code, String admin, Boolean accepted) {
		if(code == null) return false;
		if(!reports.containsKey(code)) return false;
		ChatReport report = reports.get(code).getKey();
		ExecutedTask task = reports.get(code).getValue();
		if(task == null) return false;
		task.cancel();
		reports.remove(code);
		Component messages = buildMessagesComponent(report.getMessages());
		Component admmsg = null;
		if(accepted) admmsg = this.ADMIN_ACCEPTED;
		else if(!accepted) admmsg = this.ADMIN_REJECTED;
		Component reason = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(report.getReason());
		if(admmsg != null) {
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
			this.plugin.getProxyServer().getPlayers().parallelStream().filter(this.plugin.hasPermAndNotLobby("adminutilities.admin.notify.chatreport")).forEach(p -> p.sendMessage(this.PREFIX.append(c)));
		}
		CommonPlayer pp = this.plugin.getProxyServer().getPlayer(report.getReporter());
		if(pp == null) return true;
		Component message = null;
		if(accepted) message = this.ACCEPTED;
		else if(!accepted) message = this.REJECTED;
		if (message != null) {
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
			pp.sendMessage(this.PREFIX.append(comp));
		}
		return true;
	}
	
	
	private void sendNewReport(String code) {
		if(code == null) return;
		if(!reports.containsKey(code)) return;
		ChatReport cr = reports.get(code).getKey();
		
		if (this.ADMIN_REPORTED == null) return;
		Component reason = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(cr.getReason());
		Component messages = buildMessagesComponent(cr.getMessages());
		Component comp = this.ADMIN_REPORTED.replaceText(Replacer.ACCUSED.rep(cr.getName())).replaceText(Replacer.REPORTER.rep(cr.getReporter()))
			.replaceText(Replacer.ID.rep(code))
			.replaceText(Replacer.DATE.rep(MiscUtil.parseOnlyDate(cr.getTimestamp())))
			.replaceText(Replacer.TIME.rep(MiscUtil.parseOnlyTime(cr.getTimestamp())))
			.replaceText(Replacer.REASON.rep(reason)).replaceText(Replacer.MESSAGES_LIST.rep(messages));
		comp = MiscUtil.deepReplace(comp, Replacer.ACCUSED.getMatch(), cr.getName());
		comp = MiscUtil.deepReplace(comp, Replacer.REPORTER.getMatch(), cr.getReporter());
		comp = MiscUtil.deepReplace(comp, Replacer.ID.getMatch(), code);
		comp = MiscUtil.deepReplace(comp, Replacer.DATE.getMatch(), MiscUtil.parseOnlyDate(cr.getTimestamp()));
		comp = MiscUtil.deepReplace(comp, Replacer.TIME.getMatch(), MiscUtil.parseOnlyTime(cr.getTimestamp()));
		comp = MiscUtil.deepReplace(comp, Replacer.MESSAGES_LIST.getMatch(), messages);
		comp = MiscUtil.deepReplace(comp, Replacer.REASON.getMatch(), reason);
		final Component c = comp;
		this.plugin.getProxyServer().getPlayers().parallelStream().filter(this.plugin.hasPermAndNotLobby("adminutilities.admin.notify.chatreport")).forEach(p -> this.PREFIX.append(c));
	}
	

}
