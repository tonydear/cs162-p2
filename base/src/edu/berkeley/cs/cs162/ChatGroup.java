package edu.berkeley.cs.cs162;

import java.sql.SQLException;
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
	
	public void addLoggedInUser(String uname, User u) {
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
		if(userList.contains(user))			//user already in group
			return false;
		if(userList.size() + 1 > MAX_USERS)		//adding user would exceed capacity
			return false;
		
		try {
			DBHandler.addToGroup(user,name);
			loggedInUsers.put(user, (User)userObj);			//add user to hashmap
			userList.add(user);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean leaveGroup(String user) {
		if(!loggedInUsers.containsKey(user))			//user was not registered with group
			return false;
		try {
			DBHandler.removeFromGroup(user,name);
			loggedInUsers.remove(user);					//remove user from hashmap
			userList.remove(user);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
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
			if (!user.acceptMsg(msg)) {
				if (!userList.contains(user)) {
					success = false;
				}
				else {
					try {
						DBHandler.writeLog(msg, user.getUsername());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (success)
			return MsgSendError.MESSAGE_SENT;
		else 
			return MsgSendError.MESSAGE_FAILED;
	}
}
