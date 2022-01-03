package me.szumielxd.adminutilities.common.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.simpleyaml.configuration.Configuration;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlConfiguration;

import com.google.common.collect.ImmutableMap;

import me.szumielxd.adminutilities.common.AdminUtilities;
import me.szumielxd.adminutilities.common.utils.MiscUtil;

public enum Config {
	
	/* MAIN */
	PREFIX("common.prefix", "&d&lA&e&lU&r &8&l»&r &7", true),
	DEBUG("common.debug", false),
	LUCKPERMS_DISPLAYNAME("common.luckperms-displayname", true),
	MAX_LAST_MESSAGES("common.max-last-messages", 10),
	LISTENED_COMMANDS("common.listened-commands", Arrays.asList( "say", "msg", "emsg", "whisper", "ewhisper", "r", "er", "reply", "ereply", "t", "et", "tell", "etell" )),
	IGNORE_NAMESPACES("common.listened-commands-ignore-namespaces", true),
	STAFF_LIST("common.staff-list", ImmutableMap.of("admin", "&4&lAdmin", "moderator", "&6&lModerator")),
	
	/* MESSAGES */
	MESSAGES_PERM_ERROR("message.perm-error", "&cNo, you can't", true),
	MESSAGES_COMMAND_ERROR("message.command-error", "&4An error occured while attempting to perform this command. Please report this to admin.", true),
	MESSAGES_CONSOLE_ERROR("message.console-error", "&cNot for console ;c", true),
	MESSAGES_DISPLAY_CONSOLE("message.display.console", "Console", true),
	
	/* ADMINCHNNEL */
	CHANNEL_FORMAT_ADMIN("channel.format.admin", "&7[&6Admin Chat&7] &c{senderDisplay} &6: &r&e{message}", true),
	CHANNEL_FORMAT_MOD("channel.format.mod", "&7[&6Mod Chat&7] &c{senderDisplay} &6: &r&e{message}", true),
	
	/* COMMANDS */
	// main
	COMMAND_MAIN_NAME("command.main.name", "adminutilities"),
	COMMAND_MAIN_ALIASES("command.main.aliases", Arrays.asList( "au", "adminutils", "admu" )),
	COMMAND_MAIN_USAGE("command.main.usage", "&cUsage: /adminutilities <reload|rl>", true),
	COMMAND_MAIN_SUB_RELOAD_EXECUTE("command.main.sub.reload.execute", "Reloading...", true),
	COMMAND_MAIN_SUB_RELOAD_ERROR("command.main.sub.reload.error", "&cAn error occured while reloading plugin. See console for more info.", true),
	COMMAND_MAIN_SUB_RELOAD_SUCCESS("command.main.sub.reload.success", "&aSuccessfully reloaded {plugin} v{version}", true),
	// helpop
	COMMAND_HELPOP_NAME("command.helpop.name", "helpop"),
	COMMAND_HELPOP_ALIASES("command.helpop.aliases", Arrays.asList()),
	COMMAND_HELPOP_USAGE("command.helpop.usage", "&cUsage: /helpop <message>", true),
	COMMAND_HELPOP_PREFIX("command.helpop.prefix", "&8[&4HELPOP&8] » &7", true),
	COMMAND_HELPOP_FORMAT_SENT("command.helpop.format.sent", "&6&l{sender}&7&l: &f&l{message}", true),
	COMMAND_HELPOP_FORMAT_RECEIVED("command.helpop.format.received", "{\"extra\":[{\"extra\":[{\"color\":\"gray\",\"text\":\"[\"},{\"color\":\"dark_red\",\"text\":\"{senderServer}\"},{\"color\":\"gray\",\"text\":\"] \"}],\"text\":\"\"},{\"bold\":true,\"color\":\"red\",\"insertion\":\"{sender}\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/helpop -r {sender} \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"extra\":[{\"color\":\"gold\",\"text\":\"{senderDisplay}\\n  \"},{\"color\":\"gray\",\"text\":\"Name: \"},{\"color\":\"aqua\",\"text\":\"{sender}\\n  \"},{\"color\":\"gray\",\"text\":\"UUID: \"},{\"color\":\"aqua\",\"text\":\"{senderUUID}\\n  \"},{\"color\":\"gray\",\"text\":\"Server: \"},{\"color\":\"aqua\",\"text\":\"{senderServer}\\n  \"},{\"color\":\"gray\",\"text\":\"Groups: \"},{\"color\":\"aqua\",\"text\":\"{senderGroups}\\n  \"},{\"color\":\"gray\",\"text\":\"Version: \"},{\"color\":\"aqua\",\"text\":\"{senderVersion}\\n  \"},{\"color\":\"gray\",\"text\":\"Locale: \"},{\"color\":\"aqua\",\"text\":\"{senderLocale}\\n  \"},{\"color\":\"gray\",\"text\":\"Forge: \"},{\"color\":\"aqua\",\"text\":\"{senderForge}\\n\\n\"},{\"color\":\"dark_gray\",\"text\":\"» \"},{\"color\":\"green\",\"text\":\"Click to reply\\n\"},{\"color\":\"dark_gray\",\"text\":\"» \"},{\"color\":\"green\",\"text\":\"Shift+Click to insert Name\"}],\"text\":\"\"}},\"text\":\"{sender}\"},{\"extra\":[{\"color\":\"gray\",\"text\":\": \"},{\"bold\":true,\"color\":\"white\",\"text\":\"{message}\"}],\"text\":\"\"}],\"text\":\"\"}", true),
	COMMAND_HELPOP_FORMAT_REPLY_SENT("command.helpop.format.reply-sent", "&7[&4{senderServer}&7] &7[&c&l{sender} &4&l-> &c&l{receiver}&r&7]: &f&l{message}", true),
	COMMAND_HELPOP_FORMAT_REPLY_RECEIVED("command.helpop.format.reply-received", "&c&l{sender}&r&7: &f&l{message}", true),
	// admlist
	COMMAND_ADMLIST_NAME("command.admlist.name", "admlist"),
	COMMAND_ADMLIST_ALIASES("command.admlist.aliases", Arrays.asList("administrators", "admins")),
	COMMAND_ADMLIST_HEADER("command.admlist.header", "\n &8&m                               &r &7Staff List &8&m                               \n"),
	COMMAND_ADMLIST_FORMAT_ONLINE("command.admlist.format-online", "&a{player}"),
	COMMAND_ADMLIST_FORMAT_OFFLINE("command.admlist.format-offline", "&7{player}"),
	COMMAND_ADMLIST_FORMAT_SEPARATOR("command.admlist.format-separator", "&7, "),
	COMMAND_ADMLIST_FORMAT_LINE("command.admlist.format-line", "{group}&r&7({count}): "),
	COMMAND_ADMLIST_FORMAT_INDENTATION("command.admlist.format-indentation", 5),
	COMMAND_ADMLIST_FOOTER("command.admlist.footer", "\n &8&m                                                                            \n"),
	// report
	COMMAND_REPORT_NAME("command.report.name", "report"),
	COMMAND_REPORT_ALIASES("command.report.aliases", Arrays.asList( "cheater" )),
	COMMAND_REPORT_USAGE("command.report.usage", "&cUsage: /report <nick> <reason>", true),
	COMMAND_REPORT_PREFIX("command.report.prefix", "&8[&4Report&8] » &7", true),
	COMMAND_REPORT_TIMEOUT("command.report.timeout", 120),
	COMMAND_REPORT_FORMAT_REPORTED("command.report.format.reported", "&aSuccessfully reported &b{accused}&a.", true),
	COMMAND_REPORT_FORMAT_ALREADY("command.report.format.already", "&b{accused} &cis already reported.", true),
	COMMAND_REPORT_FORMAT_TIMEOUT("command.report.format.timeout", "&cYour report has expired! (2min)", true),
	COMMAND_REPORT_FORMAT_TOOK("command.report.format.took", "&b{admin} &atook &7your report for &b{accused}.", true),
	COMMAND_REPORT_FORMAT_ADMIN_REPORTED("command.report.format.admin-reported", "{\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tempban {accused} \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"extra\":[{\"color\":\"green\",\"text\":\"Report for {accused} ({id}) [{time}]\\n\"},{\"color\":\"aqua\",\"text\":\"Click on the mark to take the report\"}],\"text\":\"\"}},\"extra\":[{\"color\":\"aqua\",\"text\":\"{reporter} \"},{\"color\":\"gray\",\"text\":\"reported \"},{\"color\":\"aqua\",\"text\":\"{accused} \"},{\"color\":\"gray\",\"text\":\"for: \"},{\"color\":\"white\",\"text\":\"{reason}\"},{\"color\":\"gray\",\"text\":\".\"},{\"text\":\" \"},{\"color\":\"gold\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/report -r {id} take\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"color\":\"green\",\"text\":\"Click to take report\"}},\"text\":\"[✔]\"}],\"text\":\"\"}", true),
	COMMAND_REPORT_FORMAT_ADMIN_TIMEOUT("command.report.format.admin-timeout", "&cReport for &b{accused} &chas expired! (2min)", true),
	COMMAND_REPORT_FORMAT_ADMIN_TOOK("command.report.format.admin-took", "&b{admin} &7took &b{reporter}&7's report for &b{accused}&7.", true),
	COMMAND_REPORT_FORMAT_ADMIN_INVALID("command.report.format.admin-invalid", "&cInvalid report code", true),
	// chatreport
	COMMAND_CHATREPORT_NAME("command.chatreport.name", "chatreport"),
	COMMAND_CHATREPORT_ALIASES("command.chatreport.aliases", Arrays.asList()),
	COMMAND_CHATREPORT_USAGE("command.chatreport.usage", "&cUsage: /chatreport <nick> <reason>", true),
	COMMAND_CHATREPORT_PREFIX("command.chatreport.prefix", "&8[&6ChatReport&8] » &7", true),
	COMMAND_CHATREPORT_TIMEOUT("command.chatreport.timeout", 120),
	COMMAND_CHATREPORT_FORMAT_REPORTED("command.chatreport.format.reported", "&aSuccessfully reported &b{accused}&a.", true),
	COMMAND_CHATREPORT_FORMAT_ALREADY("command.chatreport.format.already", "&b{accused} &cis already reported.", true),
	COMMAND_CHATREPORT_FORMAT_MESSAGES_LINE("command.chatreport.format.messages-line", "&7 - &3[{time}] &7{message}", true),
	COMMAND_CHATREPORT_FORMAT_TIMEOUT("command.chatreport.format.timeout", "&cYour report has expired! (2min)", true),
	COMMAND_CHATREPORT_FORMAT_ACCEPTED("command.chatreport.format.accepted", "&b{admin} &aaccepted &7your report for &b{accused}.", true),
	COMMAND_CHATREPORT_FORMAT_REJECTED("command.chatreport.format.rejected", "&b{admin} &4rejected &7your report for &b{accused}.", true),
	COMMAND_CHATREPORT_FORMAT_ADMIN_REPORTED("command.chatreport.format.admin-reported", "{\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tempmute {accused} \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"extra\":[{\"color\":\"green\",\"text\":\"Report for {accused} ({id}) [{time}]\\n{messages}\\n\\n\"},{\"color\":\"aqua\",\"text\":\"Click on the appropriate mark to resolve the report\"}],\"text\":\"\"}},\"extra\":[{\"color\":\"aqua\",\"text\":\"{reporter} \"},{\"color\":\"gray\",\"text\":\"reported \"},{\"color\":\"aqua\",\"text\":\"{accused} \"},{\"color\":\"gray\",\"text\":\"for: \"},{\"color\":\"white\",\"text\":\"{reason}\"},{\"color\":\"gray\",\"text\":\".\"},{\"text\":\" \"},{\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/chatreport -r {id} accept\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"color\":\"green\",\"text\":\"Click to accept\"}},\"text\":\"[✔]\"},{\"text\":\" \"},{\"color\":\"dark_red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/chatreport -r {id} reject\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"color\":\"dark_red\",\"text\":\"Click to reject\"}},\"text\":\"[✘]\"}],\"text\":\"\"}", true),
	COMMAND_CHATREPORT_FORMAT_ADMIN_TIMEOUT("command.chatreport.format.admin-timeout", "&cReport for &b{accused} &chas expired! (2min)", true),
	COMMAND_CHATREPORT_FORMAT_ADMIN_ACCEPTED("command.chatreport.format.admin-accepted", "&b{admin} &aaccepted &b{reporter}&7's report for &b{accused}&7.", true),
	COMMAND_CHATREPORT_FORMAT_ADMIN_REJECTED("command.chatreport.format.admin-rejected", "&b{admin} &4rejected &b{reporter}&7's report for &b{accused}&7.", true),
	COMMAND_CHATREPORT_FORMAT_ADMIN_INVALID("command.chatreport.format.admin-invalid", "&cInvalid report code", true),
	// adminchat
	COMMAND_ADMINCHAT_NAME("command.adminchat.name", "adminchat"),
	COMMAND_ADMINCHAT_ALIASES("command.adminchat.aliases", Arrays.asList( "ac", "achat" )),
	COMMAND_ADMINCHAT_USAGE("command.adminchat.usage", "&cUsage: /adminchat <message>", true),
	COMMAND_ADMINCHAT_CHAT_ALIAS("command.adminchat.chat-alias", "@", true),
	// adminchat
	COMMAND_MODCHAT_NAME("command.modchat.name", "modchat"),
	COMMAND_MODCHAT_ALIASES("command.modchat.aliases", Arrays.asList( "mc", "mchat" )),
	COMMAND_MODCHAT_USAGE("command.modchat.usage", "&cUsage: /modchat <message>", true),
	COMMAND_MODCHAT_CHAT_ALIAS("command.modchat.chat-alias", "%", true),
	;
	
	
	
	//////////////////////////////////////////////////////////////////////
	
	private final String path;
	private List<String> texts;
	private String text;
	private int number;
	private boolean bool;
	private Map<String, ?> map;
	private boolean colored = false;
	private Class<?> type;
	
	
	private Config(String path, String text) {
		this(path, text, false);
	}
	private Config(String path, String text, boolean colored) {
		this.path = path;
		this.colored = colored;
		setValue(text);
	}
	private Config(String path, List<String> texts) {
		this(path, texts, false);
	}
	private Config(String path, List<String> texts, boolean colored) {
		this.path = path;
		this.colored = colored;
		setValue(texts);
	}
	private Config(String path, int number) {
		this.path = path;
		setValue(number);
	}
	private Config(String path, boolean bool) {
		this.path = path;
		setValue(bool);
	}
	private Config(String path, Map<String, Object> valueMap) {
		this.path = path;
		setValue(valueMap);
	}
	
	
	
	//////////////////////////////////////////////////////////////////////
	
	public void setValue(String text) {
		this.type = String.class;
		this.text = text;
		this.texts = new ArrayList<>(Arrays.asList(new String[] { this.text }));
		this.number = text.length();
		this.bool = !text.isEmpty();
		this.map = new HashMap<>();
	}
	public void setValue(List<String> texts) {
		this.type = String[].class;
		this.text = String.join(", ", texts);
		this.texts = texts;
		this.number = texts.size();
		this.bool = !texts.isEmpty();
		this.map = texts.stream().collect(Collectors.toMap(v -> Integer.toString(texts.indexOf(v)), v -> v));
	}
	public void setValue(int number) {
		this.type = Integer.class;
		this.text = Integer.toString(number);
		this.texts = new ArrayList<>(Arrays.asList(new String[] { this.text }));
		this.number = number;
		this.bool = number > 0;
		this.map = new HashMap<>();
	}
	public void setValue(boolean bool) {
		this.type = Boolean.class;
		this.text = Boolean.toString(bool);
		this.texts = new ArrayList<>(Arrays.asList(new String[] { this.text }));
		this.number = bool? 1 : 0;
		this.bool = bool;
		this.map = new HashMap<>();
	}
	public <T> void setValue(Map<String, T> valueMap) {
		this.type = Map.class;
		this.text = valueMap.toString();
		this.texts = valueMap.values().stream().map(v -> v.toString()).collect(Collectors.toList());
		this.number = valueMap.size();
		this.bool = !valueMap.isEmpty();
		this.map = valueMap;
	}
	
	
	public String getString() {
		return this.text;
	}
	public String toString() {
		return this.text;
	}
	public List<String> getStringList() {
		return new ArrayList<>(this.texts);
	}
	public int getInt() {
		return this.number;
	}
	public boolean getBoolean() {
		return this.bool;
	}
	public Map<String, ?> getValueMap() {
		return this.map;
	}
	public boolean isColored() {
		return this.colored;
	}
	public Class<?> getType() {
		return this.type;
	}
	public String getPath() {
		return this.path;
	}
	
	
	
	//////////////////////////////////////////////////////////////////////
	
	public static void load(File file, AdminUtilities plugin) {
		plugin.getLogger().info("Loading configuration from '" + file.getName() + "'");
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		try {
			if(!file.exists()) file.createNewFile();
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			if(loadConfig(config) > 0) config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static int loadConfig(Configuration config) {
		int modify = 0;
		for (Config val : Config.values()) {
			if(!config.contains(val.getPath())) modify++;
			if (val.getType().equals(String.class)) {
				if (val.isColored())val.setValue(getColoredStringOrSetDefault(config, val.getPath(), val.getString()));
				else val.setValue(getStringOrSetDefault(config, val.getPath(), val.getString()));
			} else if (val.getType().equals(String[].class)) {
				if (val.isColored())val.setValue(getColoredStringListOrSetDefault(config, val.getPath(), val.getStringList()));
				else val.setValue(getStringListOrSetDefault(config, val.getPath(), val.getStringList()));
			} else if (val.getType().equals(Integer.class)) val.setValue(getIntOrSetDefault(config, val.getPath(), val.getInt()));
			else if (val.getType().equals(Boolean.class)) val.setValue(getBooleanOrSetDefault(config, val.getPath(), val.getBoolean()));
			else if (val.getType().equals(Map.class)) val.setValue(getMapOrSetDefault(config, val.getPath(), val.getValueMap()));
		}
		return modify;
	}
	
	/*private static <T> T getOrSetDefault(Configuration config, String path, T def) {
		SZChatFilterAPI.get().getPlugin().debug("Config::getOrSetDefault -> "+config.contains(path)+","+Map.class.isInstance(def)+","+!config.getSection(path).getKeys().isEmpty()+","+def.getClass().isInstance(config.get(path)));
		if (config.contains(path) && def.getClass().isInstance(config.get(path))) return config.get(path, def);
		config.set(path, def);
		return def;
	}*/
	
	@SuppressWarnings("unchecked")
	private static <T> Map<String,T> getMapOrSetDefault(Configuration config, String path, Map<String,T> def) {
		if (config.contains(path)) {
			Map<String,T> map = new HashMap<>();
			ConfigurationSection section = config.getConfigurationSection(path);
			for (String key : section.getKeys(false)) {
				map.put(key, (T) section.get(key));
			}
			return map;
		}
		config.set(path, def);
		return def;
	}
	
	private static int getIntOrSetDefault(Configuration config, String path, int def) {
		if (config.contains(path)) return config.getInt(path);
		config.set(path, def);
		return def;
	}
	
	private static boolean getBooleanOrSetDefault(Configuration config, String path, boolean def) {
		if (config.contains(path)) return config.getBoolean(path);
		config.set(path, def);
		return def;
	}
	
	private static String getStringOrSetDefault(Configuration config, String path, String def) {
		if (config.contains(path)) return config.getString(path);
		config.set(path, def);
		return def;
	}
	
	private static String getColoredStringOrSetDefault(Configuration config, String path, String def) {
		String str = MiscUtil.translateAlternateColorCodes('&', getStringOrSetDefault(config, path, def.replace('§', '&')));
		return str;
	}
	
	private static ArrayList<String> getStringListOrSetDefault(Configuration config, String path, List<String> def) {
		if(config.contains(path)) return new ArrayList<>(config.getStringList(path));
		config.set(path, def);
		return new ArrayList<>(def);
	}
	
	private static ArrayList<String> getColoredStringListOrSetDefault(Configuration config, String path, List<String> def) {
		ArrayList<String> list = getStringListOrSetDefault(config, path, def.stream().map(str -> str.replace('§', '&')).collect(Collectors.toCollection(ArrayList::new)));
		return list.stream().map((str) -> MiscUtil.translateAlternateColorCodes('&', str))
				.collect(Collectors.toCollection(ArrayList::new));
	}

}
