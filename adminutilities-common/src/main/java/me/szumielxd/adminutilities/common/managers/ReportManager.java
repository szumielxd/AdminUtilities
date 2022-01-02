package me.szumielxd.adminutilities.common.managers;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.data.OtherReport;
import me.szumielxd.adminutilities.common.objects.CommonPlayer;
import me.szumielxd.adminutilities.common.objects.CommonScheduler.ExecutedTask;
import me.szumielxd.adminutilities.common.utils.MiscUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ReportManager {
	
	
	private HashMap<String, Entry<OtherReport, ExecutedTask>> reports = new HashMap<>();
	public HashMap<String, Entry<OtherReport, ExecutedTask>> getActiveReports(){
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
		ID("id");
		
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
	private final Component TOOK;
	private final Component ADMIN_REPORTED;
	private final Component ADMIN_TIMEOUT;
	private final Component ADMIN_TOOK;
	
	
	private final AdminUtilities plugin;
	
	
	public ReportManager(AdminUtilities plugin) {
		this.plugin = plugin;
		this.PREFIX = MiscUtil.parseComponent(Config.COMMAND_REPORT_PREFIX.getString(), false);
		this.TIMEOUT = MiscUtil.parseComponent(Config.COMMAND_REPORT_FORMAT_TIMEOUT.getString(), true);
		this.TOOK = MiscUtil.parseComponent(Config.COMMAND_REPORT_FORMAT_TOOK.getString(), true);
		this.ADMIN_REPORTED = MiscUtil.parseComponent(Config.COMMAND_REPORT_FORMAT_ADMIN_REPORTED.getString(), true);
		this.ADMIN_TIMEOUT = MiscUtil.parseComponent(Config.COMMAND_REPORT_FORMAT_ADMIN_TIMEOUT.getString(), true);
		this.ADMIN_TOOK = MiscUtil.parseComponent(Config.COMMAND_REPORT_FORMAT_ADMIN_TOOK.getString(), true);
	}	
	
	
	public boolean registerReport(final OtherReport report) {
		
		ArrayList<String> keys = new ArrayList<String>(reports.keySet());
		HashMap<String, Entry<OtherReport, ExecutedTask>> map = new HashMap<>(reports);
		if(!keys.isEmpty()) for(String code : keys) {
			OtherReport cr = map.get(code).getKey();
			if(cr.getName().equals(report.getName())) return false;
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
		}, Config.COMMAND_REPORT_TIMEOUT.getInt(), TimeUnit.SECONDS);
		reports.put(code, new SimpleEntry<>(report, task));
		sendNewReport(code);
		return true;
		
	}
	
	
	public boolean unregisterReport(final String code, String admin) {
		if(code == null) return false;
		if(!reports.containsKey(code)) return false;
		OtherReport report = reports.get(code).getKey();
		ExecutedTask task = reports.get(code).getValue();
		if(task == null) return false;
		task.cancel();
		reports.remove(code);
		Component admmsg = this.ADMIN_TOOK;
		Component reason = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(report.getReason());
		if(admmsg != null) {
			Component comp = admmsg.replaceText(Replacer.ACCUSED.rep(report.getName())).replaceText(Replacer.REPORTER.rep(report.getReporter()))
					.replaceText(Replacer.ADMIN.rep(admin)).replaceText(Replacer.ID.rep(code))
					.replaceText(Replacer.DATE.rep(MiscUtil.parseOnlyDate(report.getTimestamp())))
					.replaceText(Replacer.TIME.rep(MiscUtil.parseOnlyTime(report.getTimestamp())))
					.replaceText(Replacer.REASON.rep(report.getReason()));
			comp = MiscUtil.deepReplace(comp, Replacer.ACCUSED.getMatch(), report.getName());
			comp = MiscUtil.deepReplace(comp, Replacer.REPORTER.getMatch(), report.getReporter());
			comp = MiscUtil.deepReplace(comp, Replacer.ADMIN.getMatch(), admin);
			comp = MiscUtil.deepReplace(comp, Replacer.ID.getMatch(), code);
			comp = MiscUtil.deepReplace(comp, Replacer.DATE.getMatch(), MiscUtil.parseOnlyDate(report.getTimestamp()));
			comp = MiscUtil.deepReplace(comp, Replacer.TIME.getMatch(), MiscUtil.parseOnlyTime(report.getTimestamp()));
			comp = MiscUtil.deepReplace(comp, Replacer.REASON.getMatch(), reason);
			final Component c = comp;
			this.plugin.getProxyServer().getPlayers().parallelStream().filter(this.plugin.hasPermAndNotLobby("adminutilities.admin.notify.chatreport")).forEach(p -> p.sendMessage(this.PREFIX.append(c)));
		}
		CommonPlayer pp = this.plugin.getProxyServer().getPlayer(report.getReporter());
		if(pp == null) return true;
		Component message = this.TOOK;
		if (message != null) {
			Component comp = message.replaceText(Replacer.ACCUSED.rep(report.getName())).replaceText(Replacer.REPORTER.rep(report.getReporter()))
					.replaceText(Replacer.ADMIN.rep(admin)).replaceText(Replacer.ID.rep(code))
					.replaceText(Replacer.DATE.rep(MiscUtil.parseOnlyDate(report.getTimestamp())))
					.replaceText(Replacer.TIME.rep(MiscUtil.parseOnlyTime(report.getTimestamp())))
					.replaceText(Replacer.REASON.rep(reason));
			comp = MiscUtil.deepReplace(comp, Replacer.ACCUSED.getMatch(), report.getName());
			comp = MiscUtil.deepReplace(comp, Replacer.REPORTER.getMatch(), report.getReporter());
			comp = MiscUtil.deepReplace(comp, Replacer.ADMIN.getMatch(), admin);
			comp = MiscUtil.deepReplace(comp, Replacer.ID.getMatch(), code);
			comp = MiscUtil.deepReplace(comp, Replacer.DATE.getMatch(), MiscUtil.parseOnlyDate(report.getTimestamp()));
			comp = MiscUtil.deepReplace(comp, Replacer.TIME.getMatch(), MiscUtil.parseOnlyTime(report.getTimestamp()));
			comp = MiscUtil.deepReplace(comp, Replacer.REASON.getMatch(), reason);
			pp.sendMessage(this.PREFIX.append(comp));
		}
		return true;
	}
	
	
	private void sendNewReport(String code) {
		if(code == null) return;
		if(!reports.containsKey(code)) return;
		OtherReport cr = reports.get(code).getKey();
		
		if (this.ADMIN_REPORTED == null) return;
		Component reason = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(cr.getReason());
		Component comp = this.ADMIN_REPORTED.replaceText(Replacer.ACCUSED.rep(cr.getName())).replaceText(Replacer.REPORTER.rep(cr.getReporter()))
			.replaceText(Replacer.ID.rep(code))
			.replaceText(Replacer.DATE.rep(MiscUtil.parseOnlyDate(cr.getTimestamp())))
			.replaceText(Replacer.TIME.rep(MiscUtil.parseOnlyTime(cr.getTimestamp())))
			.replaceText(Replacer.REASON.rep(cr.getReason()));
		comp = MiscUtil.deepReplace(comp, Replacer.ACCUSED.getMatch(), cr.getName());
		comp = MiscUtil.deepReplace(comp, Replacer.REPORTER.getMatch(), cr.getReporter());
		comp = MiscUtil.deepReplace(comp, Replacer.ID.getMatch(), code);
		comp = MiscUtil.deepReplace(comp, Replacer.DATE.getMatch(), MiscUtil.parseOnlyDate(cr.getTimestamp()));
		comp = MiscUtil.deepReplace(comp, Replacer.TIME.getMatch(), MiscUtil.parseOnlyTime(cr.getTimestamp()));
		comp = MiscUtil.deepReplace(comp, Replacer.REASON.getMatch(), reason);
		final Component c = comp;
		this.plugin.getProxyServer().getPlayers().parallelStream().filter(this.plugin.hasPermAndNotLobby("adminutilities.admin.notify.chatreport")).forEach(p -> p.sendMessage(this.PREFIX.append(c)));
	}
	

}
