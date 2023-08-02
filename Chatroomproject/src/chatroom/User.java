package chatroom;

import javax.swing.Timer;

public class User {

	private String userName;

	private Boolean active;
	private int timeinc;
	private String role;
	private String ipaddress;
	private String port;
	private Timer timer;
	private Timer messagetimer;

	private int messagetimerinc;

	public User(String userName, Boolean active, int timeinc, String role, String ipaddress, String port, Timer timer,
			Timer messagetimer, int messagetimerinc) {
		super();
		this.userName = userName;
		this.active = active;
		this.timeinc = timeinc;
		this.role = role;
		this.ipaddress = ipaddress;
		this.port = port;
		this.timer = timer;
		this.messagetimer = messagetimer;
		this.messagetimerinc = messagetimerinc;
	}

	public int getMessagetimerinc() {
		return messagetimerinc;
	}

	public void setMessagetimerinc(int messagetimerinc) {
		this.messagetimerinc = messagetimerinc;
	}

	public Timer getMessagetimer() {
		return messagetimer;
	}

	public void setMessagetimer(Timer messagetimer) {
		this.messagetimer = messagetimer;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public int getTimeinc() {
		return timeinc;
	}

	public void setTimeinc(int timeinc) {
		this.timeinc = timeinc;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
