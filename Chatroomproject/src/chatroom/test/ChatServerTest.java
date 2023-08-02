package chatroom.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.Timer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import chatroom.User;

class ChatServerTest {
	public static User user;
	public static User user2;
	public static User user3;
	public String name;
	public String role;
	public String port;
	public static String portstring;
	public static String coordstring;
	public String ip;
	public static int timeinc;
	public static int messagetimerinc;
	public static Timer timer;
	public static PrintWriter out;
	protected static Timestamp timestamp;
	public static Socket socket;
	public static Scanner in;
	public static Timer messagetimer;
	protected static List<User> users = new ArrayList<>();

	@BeforeAll
	static void testSetup() {
		
		portstring = "59001";
		coordstring = "Co-ordinator";
		
		user = new User("Joe", true, 5, coordstring, "127.0.0.1", portstring, timer, messagetimer, messagetimerinc);
		users.add(user);
		user2 = new User("Sam", true, 5, "User", "127.0.0.2", portstring, timer, messagetimer, messagetimerinc);
		users.add(user);
		user3 = new User("Phil", true, 5, "User", "127.0.0.3", portstring, timer, messagetimer, messagetimerinc);
		users.add(user);
	}

	private static Boolean coordinatorcheckbool(User user) {
		Boolean coordcheck;
		if (coordstring.equals(user.getRole())) {
			coordcheck = true;
		} else {
			coordcheck = false;
		}
		return coordcheck;

	}

	private static void timerStart(User user, int seconds) {
		user.setTimeinc(seconds);
		user.setActive(true);
		
	}

	@Test
	void testShouldReturnTrueIfCoordinator() {
		assertTrue(coordinatorcheckbool(user));
	}

	@Test
	void testIncrementShouldActivateMethodWhenZero() {
		int timeincrement = user.getTimeinc();
		timeincrement--;
		user.setTimeinc(timeincrement);
	
		String compare = "TIMER" + user.getTimeinc();
		if (user.getTimeinc() == 0) {
			assertEquals(0, timeincrement);
			assertEquals("TIMER 0", compare);
	
		}
	}

	@Test
	void testCoordinatorShouldBeAssignedToIndexZero() {
		for (User person : users) {
			if (users.indexOf(person) == 0) {
				person.setRole(coordstring);
			} else {
				person.setRole("User");
			}
		}
		String username = users.get(0).getUserName();
		assertEquals("Joe", username);
	}

	@Test
	void testCoordinatorShouldReceiveDoubleTimeOfUser() {
		if (Boolean.TRUE.equals(coordinatorcheckbool(user))) {
			timerStart(user, 20);
		} else {
			timerStart(user, 10);
		}
	
		int timereceived = user.getTimeinc();
		assertEquals(20, timereceived);
	}
	
	@Test
	void testGettersShouldReturnUserInfo() {
		
		name = user.getUserName();
		role = user.getRole();
		port = user.getPort();
		ip = user.getIpaddress();
		
		assertEquals("Joe", name);
		assertEquals(coordstring, role);
		assertEquals(portstring, port);
		assertEquals("127.0.0.1", ip);
	}
	
	@Test
	void testGettersShouldReturnUser2Info() {
		
		name = user2.getUserName();
		role = user2.getRole();
		port = user2.getPort();
		ip = user2.getIpaddress();
		
		assertEquals("Sam", name);
		assertEquals("User", role);
		assertEquals(portstring, port);
		assertEquals("127.0.0.2", ip);
	}
	
	@Test
	void testGettersShouldReturnUser3Info() {
		
		name = user3.getUserName();
		role = user3.getRole();
		port = user3.getPort();
		ip = user3.getIpaddress();
		
		assertEquals("Phil", name);
		assertEquals("User", role);
		assertEquals(portstring, port);
		assertEquals("127.0.0.3", ip);
	}
	
}






