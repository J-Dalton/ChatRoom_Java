package chatroom.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import chatroom.ChatClient;

class ChatClientTest {
	public String line;

	@Test
	void testShouldActivateIfStatementWhereSubstringMatchesSubmitname() {
		line = "SUBMITNAMEJoe";
		if (line.startsWith("SUBMITNAME")) {
			assertEquals("Joe", line.substring(10));
		}
	}
	@Test
	void testShouldActivateIfStatementWhereSubstringMatchesMessage() {
		line = "MESSAGEhi everyone";
		if (line.startsWith("MESSAGE")) {
			assertEquals("hi everyone", line.substring(7));
		}
	}
	@Test
	void testShouldActivateIfStatementWhereSubstringMatchesUser() {
		line = "USERPhil";
		if (line.startsWith("USER")) {
			assertEquals("Phil", line.substring(4));
		}
	}
	@Test
	void testShouldActivateIfStatementWhereSubstringMatchesStatus() {
		line = "STATUSCo-ordinator";
		if (line.startsWith("STATUS")) {
			assertEquals("Co-ordinator", line.substring(6));
		}
	}
	@Test
	void testMain() {
		ChatClient client = new ChatClient("127.0.0.1");
		assertNotNull(client);
	}
	

}
