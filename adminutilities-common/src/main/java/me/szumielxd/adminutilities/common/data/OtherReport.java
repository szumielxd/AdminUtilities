package me.szumielxd.adminutilities.common.data;

public class OtherReport {
	
	
	private Long timestamp;
	private String name;
	private String reporter;
	private String reason;
	
	
	public OtherReport(String accused, String reporter, String reason) {
		if(reporter == null) throw new NullPointerException("reporter cannot be null");
		if(accused == null) throw new NullPointerException("accused cannot be null");
		if(reason == null) throw new NullPointerException("reason cannot be null");
		this.name = accused;
		this.reporter = reporter;
		this.reason = reason;
		this.timestamp = System.currentTimeMillis();
	}
	
	
	public String getName() {
		return this.name;
	}
	
	public Long getTimestamp() {
		return this.timestamp;
	}
	
	public String getReporter() {
		return new String(this.reporter);
	}
	
	public String getReason() {
		return new String(this.reason);
	}
	

}
