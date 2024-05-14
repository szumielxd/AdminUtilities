package me.szumielxd.adminutilities.common.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import me.szumielxd.adminutilities.common.objects.CommonPlayer;

public class ChatReport {
	
	
	private Long timestamp;
	private String name;
	private String reporter;
	private String reason;
	private List<Entry<Long, String>> messages;
	private List<CommonPlayer> admins = new LinkedList<>();
	
	
	public ChatReport(ChatPlayer cp, String reporter, String reason) {
		if(reporter == null) throw new NullPointerException("reporter cannot be null");
		if(cp == null) throw new NullPointerException("cp cannot be null");
		if(reason == null) throw new NullPointerException("reason cannot be null");
		this.name = cp.getName();
		this.reporter = reporter;
		this.reason = reason;
		this.timestamp = System.currentTimeMillis();
		this.messages = cp.getLastMessages();
	}
	
	
	public String getName() {
		return this.name;
	}
	
	public Long getTimestamp() {
		return this.timestamp;
	}
	
	public List<Entry<Long, String>> getMessages(){
		return new ArrayList<>(this.messages);
	}
	
	public String getReporter() {
		return this.reporter;
	}
	
	public String getReason() {
		return this.reason;
	}
	
	public List<CommonPlayer> getAdmins() {
		return this.admins;
	}
	

}
