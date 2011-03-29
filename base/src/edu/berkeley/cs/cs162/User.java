package edu.berkeley.cs.cs162;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class User extends BaseUser {
	
	private ChatServer server;
	private Socket mySocket;
	private ObjectInputStream received;
	private ObjectOutputStream sent;
	private Thread receiver; 
	private String username;
	private List<String> groupsJoined;
	private Map<String, ChatLog> chatlogs;
	private Queue<MessageJob> toSend;
	private ReentrantReadWriteLock sendLock;
	private volatile boolean loggedOff;
	private final static int MAX_SEND = 10000;
	
	public User(ChatServer server, String username) {
		this.server = server;
		this.username = username;
		groupsJoined = new LinkedList<String>();
		chatlogs = new HashMap<String, ChatLog>();
		toSend = new LinkedList<MessageJob>();
		sendLock = new ReentrantReadWriteLock(true);
	}
	
	public boolean setSocket(Socket socket){ 
		this.mySocket = socket;
		try {
			received = new ObjectInputStream(mySocket.getInputStream());
			sent = new ObjectOutputStream(mySocket.getOutputStream());
			receiver = new Thread(){
	            @Override
	            public void run(){
	            	while(true){
	            		processCommand();
	            	}
	            }
	        };
	        receiver.start();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}		
		return true;
	}
	
	public Socket getSocket(){ return mySocket;}
	
	public String getUsername() {
		return username;
	}
	
	public List<String> getUserGroups() {
		return groupsJoined;
	}
	
	public Set<String> getAllUsers() {
		return server.getUsers();
	}
	
	public Set<String> getAllGroups() { 
		return server.getGroups();
	}
	
	public int getNumUsers() {
		return server.getNumUsers();
	}
	
	public int getNumGroups() {
		return server.getNumGroups();
	}
	
	public void addToGroups(String group) {
		groupsJoined.add(group);
	}
	
	public void removeFromGroups(String group) {
		groupsJoined.remove(group);
	}
	
	public ChatLog getLog(String name){
		if(chatlogs.containsKey(name)){
			return chatlogs.get(name);
		}
		return null;
	}
	
	public Map<String, ChatLog> getLogs() {
		return chatlogs;
	}
	
	public void send(String dest, String msg, int sqn) {
		sendLock.writeLock().lock();
		if(loggedOff || toSend.size() >= MAX_SEND) {
			String timestamp = Long.toString(System.currentTimeMillis()/1000);
			String formattedMsg = username + " " + dest + " " + timestamp+ " " + sqn; 
			sqn++;
			TestChatServer.logUserSendMsg(username, formattedMsg);
			TestChatServer.logChatServerDropMsg(formattedMsg, new Date());
			TransportObject toSend = new TransportObject(Command.send,sqn,ServerReply.FAIL);
			try {
				sent.writeObject(toSend);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendLock.writeLock().unlock();
			return;
		}
		String timestamp = Long.toString(System.currentTimeMillis()/1000);
		MessageJob msgJob = new MessageJob(dest,msg,sqn,timestamp);
		String formattedMsg = username + " " + dest + " " + timestamp+ " " + sqn; 

		TestChatServer.logUserSendMsg(username, formattedMsg);
		sqn++;
		toSend.add(msgJob);
		sendLock.writeLock().unlock();
	}
	
	public boolean acceptMsg(Message msg) {
		logRecvMsg(msg);
		TestChatServer.logUserMsgRecvd(username, msg.toString(), new Date());
		TransportObject toSend = new TransportObject(Command.send,msg.getDest(),msg.getSQN(),msg.getContent());
		try {
			sent.writeObject(toSend);
		} catch (Exception e) {
			return false;
		}
		msgReceived(msg.getSource()+"\t"+msg.getDest()+"\t"+msg.getSQN()+"\t"+msg.getContent());
		return true;
	}
	
	@Override
	public void msgReceived(String msg) {
		System.out.println(msg);
	}

	private void logRecvMsg(Message msg) {
		// Add to chatlog
		ChatLog log;
		String reference;
		
		if (msg.isFromGroup())
			reference = msg.getDest();
		else
			reference = msg.getSource();

		if (chatlogs.containsKey(reference))
			log = chatlogs.get(reference);
		else {
			if (msg.isFromGroup())
				log = new ChatLog(msg.getSource(), this, msg.getDest());
			else
				log = new ChatLog(msg.getSource(), this);
			
			chatlogs.put(reference, log);
		}
		
		log.add(msg);
	}
	
	public void logoff() {
		loggedOff = true;
	}
	

	public void sendClientAck (MsgSendError status,MessageJob msgJob) {
		TransportObject toSend = null;
		if (status.equals(MsgSendError.MESSAGE_SENT)) {
			toSend = new TransportObject(Command.send,msgJob.sqn,ServerReply.OK);
		} else if (status.equals(MsgSendError.INVALID_DEST)) {
			toSend = new TransportObject(Command.send,msgJob.sqn,ServerReply.BAD_DEST);
		} else if (status.equals(MsgSendError.NOT_IN_GROUP) || status.equals(MsgSendError.INVALID_SOURCE)) {
			toSend = new TransportObject(Command.send,msgJob.sqn,ServerReply.FAIL);
		} else if(status.equals(MsgSendError.MESSAGE_FAILED)){
			toSend = new TransportObject(ServerReply.sendack,msgJob.sqn);
		}
		try {
			sent.writeObject(toSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void logoffAck() {
		try {
			server.logoff(username);
			TransportObject logoutAck = new TransportObject(Command.logout, ServerReply.OK);
			sent.writeObject(logoutAck);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		try {
			server.logoff(username);
			TransportObject disconnAck = new TransportObject(Command.disconnect, ServerReply.OK);
			sent.writeObject(disconnAck);
			mySocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while(!loggedOff){
			sendLock.writeLock().lock();
			if (!toSend.isEmpty()) {
				MessageJob msgJob = toSend.poll();
				MsgSendError msgStatus = server.processMessage(username, msgJob.dest, msgJob.msg, msgJob.sqn, msgJob.timestamp);
				sendClientAck(msgStatus,msgJob);
			}
			sendLock.writeLock().unlock();
		}
		sendLock.writeLock().lock();
		while (!toSend.isEmpty()) {
			MessageJob msgJob = toSend.poll();
			String formattedMsg = username + " " + msgJob.dest + " " + System.currentTimeMillis()/1000 + "\t" + msgJob.sqn;
			TestChatServer.logChatServerDropMsg(formattedMsg, new Date());
		}
		sendLock.writeLock().unlock();
		for(String group : groupsJoined) {
			TestChatServer.logUserLeaveGroup(group, username, new Date());
		}
		groupsJoined.clear();
		TestChatServer.logUserLogout(username, new Date());
	}
	
	public void processCommand() {
		TransportObject recv = null;
		try {
			recv = (TransportObject) received.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (recv == null)
			return;
		
		if (recv.getCommand() == Command.disconnect)
			disconnect();
		else if (recv.getCommand() == Command.login)
			server.login(username);
		else if (recv.getCommand() == Command.logout) {
			server.logoff(username);
			server.startNewTimer(mySocket);
		}
		else if (recv.getCommand() == Command.join)
			server.joinGroup(this, recv.getGname());
		else if (recv.getCommand() == Command.leave)
			server.leaveGroup(this, recv.getGname());
		else if (recv.getCommand() == Command.send)
			send(recv.getDest(), recv.getMessage());
	}
}
