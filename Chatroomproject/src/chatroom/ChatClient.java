package chatroom;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Random;
import java.util.Scanner;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public class ChatClient {

	String serverAddress;
	Scanner in;
	PrintWriter out;
	JFrame frame = new JFrame("Chatter");
	JTextField textField = new JTextField(40);
	JTextArea messageArea = new JTextArea(16, 40);
	JTextArea userlist = new JTextArea(16, 13);
	JTextArea portlist = new JTextArea(16, 7);
	JTextArea statuslist = new JTextArea(16, 7);
	JTextArea iplist = new JTextArea(16, 7);
	JTextArea userinfo = new JTextArea(16, 10);
	JButton refresh = new JButton("Refresh Users");
	JButton coaway = new JButton("");
	JLabel timerlabel = new JLabel("Inactivity Timer");
	JLabel timerlabel2 = new JLabel("Message Timer");
	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	Timer timer;
	String status;
	Font font;
	Object[] options = { "Retain Co-ordinator", "Return as User", "Quit" };
	Random random = new Random();

	public ChatClient(String serverAddress) {
		this.serverAddress = serverAddress;

		frame.setLayout(new GridLayout(2, 5));
		textField.setEditable(false);
		messageArea.setEditable(false);
		userlist.setEditable(false);
		portlist.setEditable(false);
		statuslist.setEditable(false);
		iplist.setEditable(false);
		refresh.setEnabled(false);
		refresh.setVisible(false);

		GridBagLayout layout = new GridBagLayout();
		frame.setLayout(layout);
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		messageArea.setWrapStyleWord(true);
		frame.add(new JScrollPane(messageArea), gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		frame.add(new JScrollPane(userlist), gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		frame.add(new JScrollPane(statuslist), gbc);

		gbc.gridx = 4;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		frame.add(new JScrollPane(portlist), gbc);

		gbc.gridx = 5;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		frame.add(new JScrollPane(iplist), gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		frame.add(new JScrollPane(textField), gbc);

		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		frame.add(new JScrollPane(timerlabel), gbc);

		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		frame.add(new JScrollPane(timerlabel2), gbc);

		gbc.gridx = 4;
		gbc.gridy = 1;
		refresh.setPreferredSize(new Dimension(90, 16));

		frame.add(refresh, gbc);
		frame.setPreferredSize(new Dimension(901, 400));
		frame.pack();

		textField.addActionListener(e -> {
			out.println(textField.getText());
			textField.setText("");
		});

		refresh.addActionListener(e -> listRefresh());
		coaway.addActionListener(e -> coordAwayDialogueBox());

	}

	private void run() throws IOException {
		JPanel panel = (JPanel) frame.getContentPane();
		panel.setFocusable(true);
		InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		im.put(KeyStroke.getKeyStroke("control C"), "cc");
		panel.getActionMap().put("cc", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
				out.println("QUIT");
			}
		});

		try {
			Socket socket = new Socket("127.0.0.1", 59001);
			in = new Scanner(socket.getInputStream());
			out = new PrintWriter(socket.getOutputStream(), true);

			refresh.setEnabled(true);
			refresh.setVisible(true);

			frame.getContentPane().setBackground(Color.decode("#529ceb"));

			while (in.hasNextLine()) {
				inOutMessageHandler(socket);
			}
		} catch (IOException e) {
			System.err.println("Operation interrupted, please restart");
		}

		finally {
			frame.setVisible(false);
			frame.dispose();
		}
	}

	private void inOutMessageHandler(Socket socket) {
		String line = in.nextLine();

		if (line.startsWith("SUBMITNAME")) {
			out.println(getName());

		} else if (line.startsWith("NAMEACCEPTED")) {
			this.frame.setTitle("Chatter - " + line.substring(13));
			textField.setEditable(true);
			senduserinfo(out, socket);

		} else if (line.startsWith("MESSAGE")) {
			messageArea.append(line.substring(8) + "\n");

		} else if (line.startsWith("DELAYTIMER1")) {
			textField.setEnabled(false);

		} else if (line.startsWith("DELAYTIMER0")) {
			textField.setEnabled(true);

		} else if (line.startsWith("USER")) {
			userlist.append((line.substring(4)) + "\n");

		} else if (line.startsWith("IPRECEIVE")) {
			iplist.append("Ip: " + (line.substring(9)) + "\n");

		} else if (line.startsWith("PORT")) {
			portlist.append("Port: " + (line.substring(4)) + "\n");

		} else if (line.startsWith("STATUS")) {
			if ("true".equals(line.substring(6))) {
				statuslist.append("Active" + "\n");
			} else {
				statuslist.append("Away.." + "\n");
			}
		} else if (line.startsWith("UPDATE")) {
			updateCoord(out);
			refresh.doClick();

		} else if (line.startsWith("AWAY")) {
			messageArea.append(line.substring(4) + "\n");

		} else if (line.startsWith("CO-AWAY")) {
			coAwayMessage();

		} else if (line.startsWith("TIMER")) {
			timerlabel.setText(line.substring(5));

		} else if (line.startsWith("DTIME")) {
			timerlabel2.setText(line.substring(5));
		}
	}

	private String getName() {
		int randomid = random.nextInt(500);
		String result = JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
				JOptionPane.PLAIN_MESSAGE);
		if (result == null) {
			result = "Anonymous" + randomid;
		}
		return result;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Pass the server IP as the sole command line argument");
			return;
		}
		ChatClient client = new ChatClient(args[0]);
		client.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		client.frame.setVisible(true);
		client.run();
	}

	private void senduserinfo(PrintWriter out, Socket socket) {
		out.println("IPSEND" + socket.getInetAddress());
		out.println("PORTSEND" + socket.getPort());
	}

	private void updateCoord(PrintWriter out) {
		out.println("CHECKCOORD");
	}

	private void coAwayMessage() {
		coaway.doClick();
	}

	private void listRefresh() {
		userlist.setText("");
		portlist.setText("");
		iplist.setText("");
		statuslist.setText("");
		out.println("REFRESH");
		frame.invalidate();
	}

	private void coordAwayDialogueBox() {
		int result = JOptionPane.showOptionDialog(frame,
				"You are the Co-ordinator but have been set to away. Retain your role, "
						+ "quit, or return as normal user.",
				"Away notification", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
				options[2]);
		if (result == JOptionPane.YES_OPTION) {
			out.println("RETAIN-COORD");
		} else if (result == JOptionPane.NO_OPTION) {
			out.println("RETURN-AS-USER");
		} else if (result == JOptionPane.CANCEL_OPTION) {
			frame.setVisible(false);
			frame.dispose();
			out.println("QUIT");
		} else if (result == JOptionPane.CLOSED_OPTION) {
			out.println("RETAIN-COORD");
		}

	}

}
