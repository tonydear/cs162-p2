import edu.berkeley.cs.cs162.*;

import org.junit.*;

import static org.junit.Assert.* ;

public class LoginTest{
	
	ChatServer server;
	
	@Before
	public void beforeEachTest() {
		System.out.println();
		server = new ChatServer();
		server.start();
	}
	
	@After
	public void afterEachTest() {
		server.shutdown();
	}
	
	@Test
	public void testAcceptedUserLogin() {
		System.out.println("Running accepted user login test");
		assertTrue(server.login("A") == LoginError.USER_ACCEPTED);
		
	}
	
	@Test
	public void testRejectedUserLogin() {
		System.out.println("Running rejected user login test");
		server.login("A");
		assertTrue(server.login("A") == LoginError.USER_REJECTED);
	}
	
	@Test
	public void testQueuedUserLogin() {
		System.out.println("Running queued user login test");
		
		//add initial 100 users
		for (int i = 0; i< 100; i++) {
			server.login(Integer.toString(i));
		}
		
		System.out.println("Adding to wait queue.");
		//fill up wait queue and make sure all are queued up
		for(int i = 0; i < 10; i++) {
			assertTrue(server.login("A" + Integer.toString(i)) == LoginError.USER_QUEUED);
		}
		
		//check to see that no more is added once wait queue is full
		assertTrue(server.login("A") == LoginError.USER_DROPPED);
		System.out.println("Finished adding to wait queue.");
		
		//check that user on wait queue is logged on once someone logs off
		server.logoff(Integer.toString(0));
		assertTrue(server.getUser("A0") != null);
	}
	
}
