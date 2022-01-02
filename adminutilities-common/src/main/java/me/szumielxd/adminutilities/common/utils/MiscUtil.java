package me.szumielxd.adminutilities.common.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.objects.CommonPlayer;
import me.szumielxd.adminutilities.common.objects.CommonSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;

public class MiscUtil {
	
	
	private static char[] randomCharset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	
	/**
	 * The special character which prefixes all chat colour codes. Use this if
	 * you need to dynamically convert colour codes from your custom format.
	 */
	public static final char COLOR_CHAR = '\u00A7';
	public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
	
	
	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
		char[] b = textToTranslate.toCharArray();
		for ( int i = 0; i < b.length - 1; i++ )
		{
			if ( b[i] == altColorChar && ALL_CODES.indexOf( b[i + 1] ) > -1 )
			{
				b[i] = COLOR_CHAR;
				b[i + 1] = Character.toLowerCase( b[i + 1] );
			}
		}
		return new String( b );
	}
	
	
	public static Component deepReplace(Component comp, final String match, final Object replacement) {
		final String rep = replacement instanceof ComponentLike? LegacyComponentSerializer.legacyAmpersand().serialize(((ComponentLike)replacement).asComponent()) : String.valueOf(replacement);
		if (comp.clickEvent() != null) {
			ClickEvent click = comp.clickEvent();
			comp = comp.clickEvent(ClickEvent.clickEvent(click.action(), click.value().replace("{"+match+"}", rep)));
		}
		if (comp.insertion() != null) comp = comp.insertion(comp.insertion().replace("{"+match+"}", rep));
		ArrayList<Component> child = new ArrayList<>(comp.children());
		if (child != null && !child.isEmpty()) {
			child.replaceAll(c -> deepReplace(c, match, replacement));
			comp = comp.children(child);
		}
		return comp;
	}
	
	
	public static Component parseComponent(String unknown, boolean emptyAsNull) {
		if (unknown == null || (unknown.isEmpty() && emptyAsNull)) return null;
		try {
			Gson gson = new Gson();
			return GsonComponentSerializer.gson().deserializeFromTree(gson.fromJson(unknown, JsonObject.class));
		} catch (JsonSyntaxException e) {
			return LegacyComponentSerializer.legacySection().deserialize(translateAlternateColorCodes('&', unknown));
		}
	}
	
	
	public static String getPlainVisibleText(Component component) {
		Objects.requireNonNull(component, "component cannot be null");
		StringBuilder sb = new StringBuilder();
		if (component instanceof TextComponent) sb.append(((TextComponent) component).content());
		if (component.children() != null) component.children().forEach(c -> sb.append(getPlainVisibleText(c)));
		return sb.toString();
	}
	
	
	public static @NotNull String getDisplayName(@NotNull CommonSender s) {
		String name;
		if(s instanceof CommonPlayer) {
			CommonPlayer p = (CommonPlayer) s;
			name = p.getDisplayName();
			if(Config.LUCKPERMS_DISPLAYNAME.getBoolean()) {
				try {
					Class.forName("net.luckperms.api.LuckPermsProvider");
					LuckPerms api = LuckPermsProvider.get();
					User user = api.getUserManager().getUser(p.getUniqueId());
					ContextManager cm = api.getContextManager();
					QueryOptions queryOptions = cm.getQueryOptions(user).orElse(cm.getStaticQueryOptions());
					CachedMetaData meta = user.getCachedData().getMetaData(queryOptions);
					name = (meta.getPrefix()!=null? meta.getPrefix() : "")+name+(meta.getSuffix()!=null? meta.getSuffix() : "");
				} catch(Exception e) {}
			}
		} else {
			name = Config.MESSAGES_DISPLAY_CONSOLE.getString();
		}
		return translateAlternateColorCodes('&', name);
	}
	
	
	public static @NotNull String getName(@NotNull CommonSender s) {
		if(s instanceof CommonPlayer) return s.getName();
		return Config.MESSAGES_DISPLAY_CONSOLE.getString();
	}
	
	
	public static String randomString(int length) {
		StringBuilder sb = new StringBuilder();
		new Random().ints(length, 0, randomCharset.length-1).forEach(cons ->{
			sb.append(randomCharset[cons]);
		});
		return sb.toString();
	}
	
	
	public static String parseOnlyDate(long timestamp) {
		return new SimpleDateFormat("dd-MM-yyyy").format(new Date(timestamp));
	}
	
	
	public static String parseOnlyTime(long timestamp) {
		return new SimpleDateFormat("HH:mm:ss").format(new Date(timestamp));
	}
	

}
