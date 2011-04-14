package edu.berkeley.cs.cs162;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChatGroup {
	private String name;
	private Set<String> userList;
	private HashMap<String, User> loggedInUsers;
	private final static int MAX_USERS = 10;
	
	ChatGroup(String initname) {
		name = initname;
		userList = new HashSet<String>();
		loggedInUsers = new HashMap<String, User>();
	}
	
	public Set<String> getAllUsers() {
		return userList;
	}
	
	public void addUser(String uname){
		userList.add(uname);
	}
	
	public void addLoggedInUser(String uname, User u)                                                                {
		loggedInUsers.put(uname, u);
	}
	
	public void removeLoggedInUser(String uname) {
		loggedInUsers.remove(uname);
	}
	
	
	public HashMap<String, User> getUserList() {
		return loggedInUsers;
	}
	
	public int getNumUsers() {
		return loggedInUsers.size();
	}
	
	public String getName() {
		return name;
	}
	
	public boolean onCreate() {
		return true;
	}
	
	public boolean onDelete() {
		return true;
	}
	
	public boolean joinGroup(String user, BaseUser userObj) {
		if(loggedInUsers.containsKey(user))			//user already in group
			return false;
		if(loggedInUsers.size() + 1 > MAX_USERS)		//adding user would exceed capacity
			return false;
		loggedInUsers.put(user, (User)userObj);			//add user to hashmap
		userList.add(user);
		DBHandler.addToGroup(user,name);
		return true;
	}
	
	public boolean leaveGroup(String user) {
		userList.remove(user);
		if(!loggedInUsers.containsKey(user))			//user was not registered with group
			return false;
		loggedInUsers.remove(user);					//remove user from hashmap
		DBHandler.removeFromGroup(user,name);
		return true;
	}
	
	public synchronized MsgSendError forwardMessage(Message msg) {
		if (! loggedInUsers.containsKey(msg.getSource()))
			return MsgSendError.NOT_IN_GROUP;
		Collection<User> users = loggedInUsers.values();
		Iterator<User> it = users.iterator();
		User user;
		boolean success = true;
		while(it.hasNext()) {
			user = it.next();
			if (!user.acceptMsg(msg))
				success = false;
		}
		if (success)
			return MsgSendError.MESSAGE_SENT;
		else 
			return MsgSendError.MESSAGE_FAILED;
	}
}
