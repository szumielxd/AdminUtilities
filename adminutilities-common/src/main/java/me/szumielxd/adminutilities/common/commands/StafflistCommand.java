package me.szumielxd.adminutilities.common.commands;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.objects.CommonSender;
import me.szumielxd.adminutilities.common.utils.MiscUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.InheritanceNode;

public class StafflistCommand extends CommonCommand {
	
	
	private final @NotNull Component HEADER;
	private final @NotNull Component FOOTER;
	private final @NotNull Component FORMAT_ONLINE;
	private final @NotNull Component FORMAT_OFFLINE;
	private final @NotNull Component FORMAT_SEPARATOR;
	private final @NotNull Component FORMAT_LINE;
	private final @NotNull TextComponent FORMAT_INDENTATION;
	private final @NotNull AdminUtilities plugin;
	private long lastUpdate = 0L;
	private @NotNull CompletableFuture<Map<String, Map<UUID, Map.Entry<String, Boolean>>>> cacheFuture;
	

	public StafflistCommand(@NotNull AdminUtilities plugin) {
		super(Config.COMMAND_ADMLIST_NAME.getString(), "adminutilities.command.admlist", Config.COMMAND_ADMLIST_ALIASES.getStringList().toArray(new String[0]));
		this.HEADER = MiscUtil.parseComponent(Config.COMMAND_ADMLIST_HEADER.getString(), false);
		this.FOOTER = MiscUtil.parseComponent(Config.COMMAND_ADMLIST_FOOTER.getString(), false);
		this.FORMAT_ONLINE = MiscUtil.parseComponent(Config.COMMAND_ADMLIST_FORMAT_ONLINE.getString(), false);
		this.FORMAT_OFFLINE = MiscUtil.parseComponent(Config.COMMAND_ADMLIST_FORMAT_OFFLINE.getString(), false);
		this.FORMAT_SEPARATOR = MiscUtil.parseComponent(Config.COMMAND_ADMLIST_FORMAT_SEPARATOR.getString(), false);
		this.FORMAT_LINE = MiscUtil.parseComponent(Config.COMMAND_ADMLIST_FORMAT_LINE.getString(), false);
		this.FORMAT_INDENTATION = Component.text(IntStream.range(0, Config.COMMAND_ADMLIST_FORMAT_INDENTATION.getInt()).mapToObj(x -> " ").collect(Collectors.joining()));
		this.plugin = plugin;
	}

	@Override
	public void execute(@NotNull CommonSender s, String[] args) {
		
		
		this.plugin.getProxyServer().getScheduler().runTask(() -> {
			try {
				TextComponent message = Component.empty().append(HEADER);
				
				Map<String, Map<UUID, Entry<String, Boolean>>> staff = getCachedStaff().get();
				int length;
				boolean first;
				final int sepLength = MiscUtil.getPlainVisibleText(this.FORMAT_SEPARATOR).length();
				if (!staff.isEmpty()) for (Map.Entry<String, Map<UUID, Entry<String, Boolean>>> e : staff.entrySet()) {
					String group = e.getKey();
					Map<UUID, Entry<String, Boolean>> users = e.getValue();
					Component groupDisplay = FORMAT_LINE
							.replaceText(rep("group", Component.empty().children(Arrays.asList(MiscUtil.parseComponent(Config.STAFF_LIST.getValueMap().get(group).toString(), false))))) // put replacement as children to avoid format inheritance from replacement
							.replaceText(rep("count", users.size()));
					message = message.append(Component.newline()).append(FORMAT_INDENTATION).append(groupDisplay);
					length = MiscUtil.getPlainVisibleText(groupDisplay).length();
					Iterator<UUID> iter = users.keySet().iterator();
					first = true;
					while (iter.hasNext()) {
						if (first) {
							first = false;
						} else {
							length += sepLength;
							message = message.append(FORMAT_SEPARATOR);
							if (length > 70) {
								// new line
								length = 0;
								message = message.append(Component.newline()).append(FORMAT_INDENTATION).append(Component.text("  "));
							}
						}
						UUID uuid = iter.next();
						Entry<String, Boolean> status = users.get(uuid);
						Component user = (status.getValue() ? FORMAT_ONLINE : FORMAT_OFFLINE).replaceText(rep("player", status.getKey()));
						length += MiscUtil.getPlainVisibleText(user).length();
						message = message.append(user);
					}
				}
				
				message = message.append(Component.newline()).append(FOOTER);
				s.sendMessage(message);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
		
	}
	
	
	private CompletableFuture<Map<String, Map<UUID, Map.Entry<String, Boolean>>>> getCachedStaff() {
		if (System.currentTimeMillis() > this.lastUpdate + 30*1000) {
			this.lastUpdate = System.currentTimeMillis();
			try {
				Class.forName("net.luckperms.api.LuckPermsProvider");
			} catch (ClassNotFoundException e2) {
				// fallback for lack of LuckPerms
				return this.cacheFuture = CompletableFuture.completedFuture(Collections.emptyMap());
			}
			final CompletableFuture<Map<String, Map<UUID, Map.Entry<String, Boolean>>>> future = new CompletableFuture<>();
			new Thread(() -> {
				try {
					Map<String, Map<UUID, Map.Entry<String, Boolean>>> staff = new HashMap<>();
					Set<String> groups = Config.STAFF_LIST.getValueMap().keySet();
					groups.forEach(g -> staff.put(g, new HashMap<>())); // for correct order
					UserManager mgr = LuckPermsProvider.get().getUserManager();
					groups.parallelStream().forEach(g -> {
						Set<UUID> users;
						try {
							users = mgr.searchAll(NodeMatcher.key(InheritanceNode.builder(g).build().getKey())).get().keySet();
							Map<UUID, Map.Entry<String, Boolean>> map = new HashMap<>();
							// lookup for names
							users.parallelStream().forEach(uuid -> {
								try {
									map.put(uuid, new AbstractMap.SimpleEntry<>(mgr.lookupUsername(uuid).get(), this.plugin.getProxyServer().getPlayer(uuid) != null));
								} catch (InterruptedException | ExecutionException e1) {
									throw new RuntimeException(e1);
								}
							});
							// sort by name
							map.entrySet().stream().sorted(Entry.comparingByValue(Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))).forEachOrdered(e -> staff.get(g).put(e.getKey(), e.getValue()));
						} catch (InterruptedException | ExecutionException e) {
							throw new RuntimeException(e);
						}
					});
					Map<String, Map<UUID, Map.Entry<String, Boolean>>> finalMap = Collections.unmodifiableMap(staff);
					future.complete(finalMap);
					this.cacheFuture = CompletableFuture.completedFuture(finalMap);
				} catch (Exception e) {
					// fallback to default future
					future.completeExceptionally(e);
					this.cacheFuture = CompletableFuture.completedFuture(Collections.emptyMap());
				}
			}, "AdminUtils - StaffList Update").start();
			this.cacheFuture = future;
		}
		return this.cacheFuture;
	}
	
	
	
	
	public TextReplacementConfig rep(String match, Object toReplace) {
		if (toReplace instanceof ComponentLike) return TextReplacementConfig.builder().matchLiteral("{"+match+"}").replacement((ComponentLike) toReplace).build();
		return TextReplacementConfig.builder().matchLiteral("{"+match+"}").replacement(String.valueOf(toReplace)).build();
	}
	

}
