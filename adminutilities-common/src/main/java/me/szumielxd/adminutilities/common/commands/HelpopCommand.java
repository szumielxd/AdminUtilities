package me.szumielxd.adminutilities.common.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.objects.CommonPlayer;
import me.szumielxd.adminutilities.common.objects.CommonSender;
import me.szumielxd.adminutilities.common.utils.MiscUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class HelpopCommand extends CommonCommand {
	
	
	private final @NotNull Component PREFIX;
	private final @NotNull Component USAGE;
	private final @NotNull Component SENT;
	private final @NotNull Component RECEIVED;
	private final @NotNull Component REPLY_SENT;
	private final @NotNull Component REPLY_RECEIVED;
	private final @NotNull AdminUtilities plugin;
	

	public HelpopCommand(@NotNull AdminUtilities plugin) {
		super(Config.COMMAND_HELPOP_NAME.getString(), "adminutilities.command.helpop", Config.COMMAND_HELPOP_ALIASES.getStringList().toArray(new String[0]));
		this.PREFIX = MiscUtil.parseComponent(Config.COMMAND_HELPOP_PREFIX.getString(), false);
		this.USAGE = MiscUtil.parseComponent(Config.COMMAND_HELPOP_USAGE.getString(), false);
		this.SENT = MiscUtil.parseComponent(Config.COMMAND_HELPOP_FORMAT_SENT.getString(), false);
		this.RECEIVED = MiscUtil.parseComponent(Config.COMMAND_HELPOP_FORMAT_RECEIVED.getString(), false);
		this.REPLY_SENT = MiscUtil.parseComponent(Config.COMMAND_HELPOP_FORMAT_REPLY_SENT.getString(), false);
		this.REPLY_RECEIVED = MiscUtil.parseComponent(Config.COMMAND_HELPOP_FORMAT_REPLY_RECEIVED.getString(), false);
		this.plugin = plugin;
	}

	@Override
	public void execute(@NotNull CommonSender s, @NotNull String[] args) {
		
		if(args.length < 1) {
			s.sendMessage(this.PREFIX.append(this.USAGE));
			return;
		}
		if (args[0].equalsIgnoreCase("-r") && s.hasPermission("adminutilities.admin.notify.helpop")) {
			if (args.length > 2) {
				CommonPlayer pp = this.plugin.getProxyServer().getPlayer(args[1]);
				if (pp != null) {
					String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
					if (s.hasPermission("adminutilities.command.helpop.format")) message = MiscUtil.translateAlternateColorCodes('&', message);
					Component msg = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(message);
					this.plugin.getProxyServer().getPlayers().parallelStream().filter(p -> p.hasPermission("adminutilities.admin.notify.helpop"))
							.forEach(p -> p.sendMessage(this.PREFIX.append(parsePlaceholders(this.REPLY_SENT, s, pp, msg))));
					pp.sendMessage(this.PREFIX.append(parsePlaceholders(this.REPLY_RECEIVED, s, pp, msg)));
					return;
				}
			}
		}
		
		if(!(s instanceof CommonPlayer)) {
			s.sendMessage(LegacyComponentSerializer.legacySection().deserialize(Config.PREFIX.getString()+Config.MESSAGES_CONSOLE_ERROR.getString()));
			return;
		}
		
		String message = String.join(" ", args);
		if (s.hasPermission("adminutilities.command.helpop.format")) message = MiscUtil.translateAlternateColorCodes('&', message);
		Component msg = LegacyComponentSerializer.legacySection().toBuilder().extractUrls().build().deserialize(message);
		this.plugin.getProxyServer().getPlayers().parallelStream().filter(p -> p.hasPermission("adminutilities.admin.notify.helpop"))
				.forEach(p -> p.sendMessage(this.PREFIX.append(parsePlaceholders(this.RECEIVED, s, null, msg))));
		s.sendMessage(this.PREFIX.append(parsePlaceholders(this.SENT, s, null, msg)));
		
	}
	
	
	private @NotNull Component parsePlaceholders(@NotNull Component comp, @NotNull CommonSender sender, @Nullable CommonSender receiver, @NotNull ComponentLike message) {
		String sDisplay = MiscUtil.getDisplayName(sender);
		String sGroups = String.join(", ", sender instanceof CommonPlayer pps ? pps.getGroups().toArray(new String[0]) : new String[0]);
		Locale sLocale = Locale.getDefault();
		int sVersion = -1;
		String sServer = "";
		UUID sUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
		boolean sForge = false;
		if (sender instanceof CommonPlayer pps) {
			sLocale = pps.locale();
			sVersion = pps.getVersion();
			sServer = pps.getWorldName();
			sUUID = pps.getUniqueId();
			sForge = pps.isModded();
		}
		comp = comp.replaceText(rep("sender", MiscUtil.getName(sender))).replaceText(rep("senderDisplay", sDisplay))
				.replaceText(rep("senderGroups", sGroups)).replaceText(rep("senderLocale", sLocale))
				.replaceText(rep("senderVersion", sVersion)).replaceText(rep("senderServer", sServer))
				.replaceText(rep("senderUUID", sUUID)).replaceText(rep("senderForge", sForge));
		comp = MiscUtil.deepReplace(comp, "sender", sender.getName());
		comp = MiscUtil.deepReplace(comp, "senderDisplay", sDisplay);
		comp = MiscUtil.deepReplace(comp, "senderGroups", sGroups);
		comp = MiscUtil.deepReplace(comp, "senderLocale", sLocale);
		comp = MiscUtil.deepReplace(comp, "senderVersion", sVersion);
		comp = MiscUtil.deepReplace(comp, "senderServer", sServer);
		comp = MiscUtil.deepReplace(comp, "senderUUID", sUUID);
		comp = MiscUtil.deepReplace(comp, "senderForge", sForge);
		
		if (receiver != null) {
			String rDisplay = MiscUtil.getDisplayName(receiver);
			String rGroups = String.join(", ", receiver instanceof CommonPlayer recp ? recp.getGroups().toArray(new String[0]) : new String[0]);
			Locale rLocale = Locale.getDefault();
			int rVersion = -1;
			String rServer = "";
			UUID rUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
			boolean rForge = false;
			if (receiver instanceof CommonPlayer ppr) {
				rLocale = ppr.locale();
				rVersion = ppr.getVersion();
				rServer = ppr.getWorldName();
				rUUID = ppr.getUniqueId();
				rForge = ppr.isModded();
			}
			comp = comp.replaceText(rep("receiver", MiscUtil.getName(receiver))).replaceText(rep("receiverDisplay", MiscUtil.getDisplayName(receiver)))
					.replaceText(rep("receiverGroups", String.join(", ", Optional.of(receiver).filter(CommonPlayer.class::isInstance).map(CommonPlayer.class::cast).map(CommonPlayer::getGroups).orElse(Collections.emptyList()))))
					.replaceText(rep("receiverLocale", rLocale))
					.replaceText(rep("receiverVersion", rVersion)).replaceText(rep("receiverServer", rServer))
					.replaceText(rep("receiverUUID", rUUID)).replaceText(rep("receiverForge", rForge));
			comp = MiscUtil.deepReplace(comp, "receiver", MiscUtil.getName(receiver));
			comp = MiscUtil.deepReplace(comp, "receiverDisplay", rDisplay);
			comp = MiscUtil.deepReplace(comp, "receiverGroups", rGroups);
			comp = MiscUtil.deepReplace(comp, "receiverLocale", rLocale);
			comp = MiscUtil.deepReplace(comp, "receiverVersion", rVersion);
			comp = MiscUtil.deepReplace(comp, "receiverServer", rServer);
			comp = MiscUtil.deepReplace(comp, "receiverUUID", rUUID);
			comp = MiscUtil.deepReplace(comp, "receiverForge", rForge);
		}
		comp = MiscUtil.deepReplace(comp, "message", message);
		return comp.replaceText(rep("message", message));
	}
	
	public @NotNull TextReplacementConfig rep(@NotNull String match, @NotNull Object toReplace) {
		if (toReplace instanceof ComponentLike rep) return TextReplacementConfig.builder().matchLiteral("{"+match+"}").replacement(rep).build();
		return TextReplacementConfig.builder().matchLiteral("{"+match+"}").replacement(String.valueOf(toReplace)).build();
	}
	

}
