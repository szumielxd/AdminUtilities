package me.szumielxd.adminutilities.common.data;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import me.szumielxd.adminutilities.common.AdminUtilitiesProvider;
import me.szumielxd.adminutilities.common.configuration.Config;
import me.szumielxd.adminutilities.common.objects.CommonPlayer;
import me.szumielxd.adminutilities.common.objects.CommonScheduler.ExecutedTask;

public class ChatPlayer {

	
	private UUID uuid;
	private String name;
	private ArrayList<Entry<Long, String>> chat = new ArrayList<>();
	
	
	private ChatPlayer(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
		chatPlayers.add(this);
	}
	
	
	public void append(Long timestamp, String message) {
		if(timestamp == null) throw new NullPointerException("date cannot be null");
		if(message == null) throw new NullPointerException("message cannot be null");
		chat.add(new SimpleEntry<>(timestamp, message));
		if(chat.size() > Config.MAX_LAST_MESSAGES.getInt()) chat.remove(0);
	}
	
	public UUID getUUID() {
		return this.uuid;
	}
	
	public String getName() {
		return new String(this.name);
	}
	
	public ArrayList<Entry<Long, String>> getLastMessages() {
		return new ArrayList<>(this.chat);
	}
	
	
	private static ArrayList<ChatPlayer> chatPlayers = new ArrayList<>();
	private static ExecutedTask task;
	
	public static void load() {
		if(task != null) task.cancel();
		task = AdminUtilitiesProvider.get().getProxyServer().getScheduler().runTaskTimer(new Runnable() {
			@Override
			public void run() {
				if(chatPlayers.isEmpty()) return;
				for(ChatPlayer cp : new ArrayList<>(chatPlayers)) {
					if(AdminUtilitiesProvider.get().getProxyServer().getPlayer(cp.getUUID()) == null) {
						chatPlayers.remove(cp);
					}
				}
				
			}
		}, 20L, 20L, TimeUnit.SECONDS);
	}
	
	public static void unload() {
		if(task == null) return;
		task.cancel();
		task = null;
	}
	
	public static ChatPlayer getChatPlayer(UUID uuid) {
		CommonPlayer pp = AdminUtilitiesProvider.get().getProxyServer().getPlayer(uuid);
		if(pp == null) return null;
		String name = pp.getName();
		if(chatPlayers.size() > 0) for(ChatPlayer cp : new ArrayList<>(chatPlayers)) {
			if(cp.getUUID().equals(uuid)) return cp;
		}
		return new ChatPlayer(uuid, name);
	}
	
	public static ChatPlayer getChatPlayer(String name) {
		CommonPlayer pp = AdminUtilitiesProvider.get().getProxyServer().getPlayer(name);
		if(pp == null) return null;
		UUID uuid = pp.getUniqueId();
		if(chatPlayers.size() > 0) for(ChatPlayer cp : new ArrayList<>(chatPlayers)) {
			if(cp.getName().equals(name)) return cp;
		}
		return new ChatPlayer(uuid, name);
	}
	
	
}
