package chatroom;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import javax.swing.Timer;

public class ChatServer {

	private static Set<String> names = new HashSet<>();
	private static Set<PrintWriter> writers = new HashSet<>();
	private static List<User> users = new ArrayList<>();
	private static final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void main(String[] args) throws Exception {
		System.out.println("The chat server is running...");
		ExecutorService pool = Executors.newFixedThreadPool(500);
		try (ServerSocket listener = new ServerSocket(59001)) {
			while (true) {
				pool.execute(new Handler(listener.accept()));

			}
		} catch(Exception e) {
			System.err.println("Exception thrown, please restart");
		} 
		
	}

	private static class Handler implements Runnable {
		private String name;
		private Socket socket;
		private Scanner in;
		private PrintWriter out;
		private User user;
		private Timer timer;
		private Timer messagetimer;
		private int timeinc;
		private static String update = "UPDATE";
		private static String message = "MESSAGE ";

		public Handler(Socket socket) {
			this.socket = socket;

			timer = new Timer(1000, null);
			timer.addActionListener(e -> timerCountdown(user, out));
			timer.setRepeats(true);

			messagetimer = new Timer(1000, null);
			messagetimer.addActionListener(e -> messageTimerCountdown(user, out));
			messagetimer.setRepeats(true);

		}

		public void run() {
			try {
				in = new Scanner(socket.getInputStream());
				out = new PrintWriter(socket.getOutputStream(), true);

				while (true) {
					out.println("SUBMITNAME");
					name = in.nextLine();
					if (name == null) {
						return;
					}
					synchronized (names) {
						if (!name.isEmpty() && !names.contains(name)) {
							names.add(name);
							timeinc = 12;

							user = new User(name, true, timeinc, "", "", "", timer, messagetimer, 3);
							users.add(user);
							break;
						}
					}
				}

				out.println("NAMEACCEPTED " + user.getUserName());
				writers.add(out);
				user.setTimeinc(timeinc);
				setCoordinator();
				timerStartRoleCheck(user);

				for (PrintWriter writer : writers) {
					writer.println(message + user.getUserName() + " has joined");
					writer.println(message + users.get(0).getUserName() + " is the Current Co-ordinator");
					writer.println(update);

				}

				while (true) {
					inoutMessageHandler(in, out, user, name);
				}
			} catch (Exception e) {
				System.out.println(e);
				Thread.currentThread().interrupt();
				
			} finally {
				userQuit(user, out, name);
				
			}
			
			try {
				socket.close();
				out.close();

			} catch (IOException e) {
				System.out.println(e);
			}
		}

		private static void inoutMessageHandler(Scanner in, PrintWriter out, User user, String name) {
			String input = in.nextLine();
			if (input.toLowerCase().startsWith("/quit")) {
				return;
			}

			if (input.toLowerCase().startsWith("/roles")) {
				sendRolesList(out);
			}

			if (input.startsWith("REFRESH")) {
				userListoclient(out);

			} else if (input.startsWith("PORTSEND")) {
				user.setPort(input.substring(8));

			} else if (input.startsWith("QUIT")) {
				userQuit(user, out, name);

			} else if (input.startsWith("RETAIN-COORD")) {
				timerStartRoleCheck(user);

			} else if (input.startsWith("RETURN-AS-USER")) {
				shiftIndex(user);
				inactiveCoordinator();

				users.get(0).setTimeinc(20);
				users.get(0).getTimer().start();

			} else if (input.startsWith("CHECKCOORD")) {
				setCoordinator();

			} else if (input.startsWith("IPSEND")) {
				user.setIpaddress(input.substring(6));
			} else {
				timerStartRoleCheck(user);
				for (PrintWriter writer : writers) {
					Timestamp messagetimestamp = new Timestamp(System.currentTimeMillis());
					writer.println(message + sdf3.format(messagetimestamp) + " - " + user.getUserName() + ": " + input);
					writer.println(update);
				}
			
				user.getMessagetimer().start();
				out.println("DELAYTIMER1");
			}
		}

		private static void userQuit(User user, PrintWriter out, String name) {
			if (out != null) {
				writers.remove(out);
			}
			if (user.getUserName() != null) {
				user.getTimer().stop();
				names.remove(name);
				users.remove(user);
				for (PrintWriter writer : writers) {
					if (Boolean.TRUE.equals(coordinatorcheckbool(user))) {
						writer.println(message + "COORDINATOR is leaving");
						writer.println(message + "New Co-ordinator assigned to " + users.get(0).getUserName());
						users.get(0).setTimeinc(20);
						users.get(0).getTimer().start();
						writer.println(update);

					} else {
						writer.println(message + user.getUserName() + " has left");
						writer.println(update);
					}
				}
			}
		}

		private static Boolean coordinatorcheckbool(User user) {
			Boolean coordcheck;
			if ("Co-ordinator".equals(user.getRole())) {
				coordcheck = true;
			} else {
				coordcheck = false;
			}
			return coordcheck;
		}

		private static void timerCountdown(User user, PrintWriter out) {
			int timeincrement = user.getTimeinc();
			timeincrement--;
			user.setTimeinc(timeincrement);
			out.println("TIMER" + user.getTimeinc());
			if (user.getTimeinc() == 0) {
				timerCountdownReachesZero(user, out);
			}
		}

		private static void messageTimerCountdown(User user, PrintWriter out) {
			int messagetimer = user.getMessagetimerinc();
			messagetimer--;
			user.setMessagetimerinc(messagetimer);
			out.println("DTIME"+ user.getMessagetimerinc());
			if (user.getMessagetimerinc() == 0) {
				
				out.println("DELAYTIMER0");
				user.getMessagetimer().stop();
				user.setMessagetimerinc(2);
			}
			
		}

		private static void timerCountdownReachesZero(User user, PrintWriter out) {
			user.setActive(false);
			user.getTimer().stop();
			if (Boolean.TRUE.equals(coordinatorcheckbool(user))) {
				if (users.size() > 1) {
					out.println("CO-AWAY");
				}

			} else {
				inactiveUser(user);
			}
		}

		private static void inactiveUser(User user) {
			for (PrintWriter writer : writers) {
				Timestamp awaytimestamp = new Timestamp(System.currentTimeMillis());
				writer.println("AWAY" + sdf3.format(awaytimestamp) + " - " + user.getUserName() + " is away");
				writer.println(update);
			}
		}

		private static void shiftIndex(User user) {
			int indexpos = users.indexOf(user);
			users.remove(indexpos);
			users.add(users.size(), user);
			setCoordinator();
		}

		private static void setCoordinator() {
			for (User person : users) {
				if (users.indexOf(person) == 0) {
					person.setRole("Co-ordinator");
				} else {
					person.setRole("User");
				}
			}
		}

		private static void inactiveCoordinator() {
			for (PrintWriter writer : writers) {
				if (users.size() > 1) {
					writer.println(
							message + "Co-ordinator is inactive, role now assigned to " + users.get(0).getUserName());
					writer.println(update);
				}
			}
		}

		private static void timerStartRoleCheck(User user) {
			if (Boolean.TRUE.equals(coordinatorcheckbool(user))) {
				timerStart(user, 20);
			} else {
				timerStart(user, 10);
			}
		}

		private static void timerStart(User user, int seconds) {
			user.setTimeinc(seconds);
			user.setActive(true);
			user.getTimer().start();
		}

		private static void userListoclient(PrintWriter writer) {

			for (int i = 0; i < users.size(); i++) {
				writer.println("USER" + users.get(i).getUserName() + " : " + users.get(i).getRole());
				writer.println("IPRECEIVE" + users.get(i).getIpaddress());
				writer.println("PORT" + users.get(i).getPort());
				writer.println("STATUS" + users.get(i).getActive());
			}
		}

		private static void sendRolesList(PrintWriter writer) {
			for (int i = 0; i < users.size(); i++) {
				writer.println(message + " User: " + users.get(i).getUserName() + "- Role: " + users.get(i).getRole());

			}
		}
	}
}
