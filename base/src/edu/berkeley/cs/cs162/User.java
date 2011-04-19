package edu.berkeley.cs.cs162;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class User extends BaseUser {

	private ChatServer server;
	private Socket mySocket;
	private ObjectInputStream received;
	private ObjectOutputStream sent;
	private Thread receiver; 
	private Thread sender;
	private String username;
	private List<String> groupsJoined;
	private Map<String, ChatLog> chatlogs;
	private BlockingQueue<MessageJob> toSend;
	private ReentrantReadWriteLock sendLock;
	private volatile boolean loggedOff;
	private final static int MAX_SEND = 10000;
	private BlockingQueue<TransportObject> queuedServerReplies;
	private boolean pendingLogoff;

	public User(ChatServer server, String username) {
		this.server = server;
		this.username = username;
		groupsJoined = new LinkedList<String>();
		chatlogs = new HashMap<String, ChatLog>();
		toSend = new ArrayBlockingQueue<MessageJob>(MAX_SEND);
		sendLock = new ReentrantReadWriteLock(true);
		pendingLogoff = false;
		queuedServerReplies = new ArrayBlockingQueue<TransportObject>(MAX_SEND);
	}

	public boolean queueReply(TransportObject reply) {
		if (!pendingLogoff) {
			return queuedServerReplies.add(reply);
		} else {
			return false;
		}
	}

	public boolean setSocket(Socket socket, ObjectInputStream receivedParam, ObjectOutputStream sentParam){ 
		this.mySocket = socket;

		this.received = receivedParam;
		this.sent = sentParam;
		sender = new Thread(){
			@Override
			public void run(){
				while(!pendingLogoff) {
					TransportObject reply = null;
					try {
						reply = queuedServerReplies.poll(3, TimeUnit.SECONDS);
						if(reply != null)
							sent.writeObject(reply);
					} catch (SocketException e) {
						System.err.println(e);
					} catch (Exception e) {
						if(reply.getCommand().equals(Command.send)) {
							User sender = (User) server.getUser(reply.getSender());
							if(sender!=null){
								TransportObject error = new TransportObject(ServerReply.sendack,reply.getSQN());
								sender.queueReply(error);
							}
						}
					}
				}
			}
			
		};
		receiver = new Thread(){
			@Override
			public void run(){
				while(!pendingLogoff){
					processCommand();
				}
			}
		};
		receiver.start();
		sender.start();
		return true;

	}

	public Socket getSocket(){ return mySocket;}

	public String getUsername() {
		return username;
	}

	public List<String> getUserGroups() {
		return groupsJoined;
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
			queueReply(toSend);
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
		TransportObject toSend = new TransportObject(ServerReply.receive,msg.getSource(),
				msg.getDest(),msg.getContent(),msg.getTimestamp());
		if(!queueReply(toSend))
			return false;
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
		pendingLogoff = true;
		
		logoffAck();
		
		while (!queuedServerReplies.isEmpty()) {
			TransportObject reply = null;
			try {
				reply = queuedServerReplies.poll();
				if(reply!=null)
					sent.writeObject(reply);
			} catch (SocketException e) {
				System.err.println(e);
			} catch (Exception e) {
				if(reply!=null&&reply.getCommand().equals(Command.send)) {
					User sender = (User) server.getUser(reply.getSender());
					if(sender!=null){
						TransportObject error = new TransportObject(ServerReply.sendack,reply.getSQN());
						sender.queueReply(error);
					}
				}
			}
		}
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
		queueReply(toSend);
	}

	public void logoffAck() {
		TransportObject logoutAck = new TransportObject(Command.logout, ServerReply.OK);
		queuedServerReplies.add(logoutAck);
	}

	public ObjectOutputStream getOutputStream() {
		return sent;
	}

	public void disconnect() {
		server.logoff(username);
		try {
			TransportObject disconnAck = new TransportObject(Command.disconnect, ServerReply.OK);
			queueReply(disconnAck);
			mySocket.close();
		} catch (IOException e) {
			System.err.print(e);
		}
	}

	public void run() {
		while(!loggedOff){
			//sendLock.writeLock().lock();
			//if (!toSend.isEmpty()) {
			MessageJob msgJob = null;
			try {
				msgJob = toSend.poll(3, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(msgJob!=null){
				MsgSendError msgStatus = server.processMessage(username, msgJob.dest, msgJob.msg, msgJob.sqn, msgJob.timestamp);
				sendClientAck(msgStatus,msgJob);
			}
			//}
			//sendLock.writeLock().unlock();
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
			disconnect();
		}
		if (recv == null)
			disconnect();	
		else if (recv.getCommand() == Command.disconnect)
			disconnect();
		else if (recv.getCommand() == Command.login) {
			TransportObject send = new TransportObject(Command.login, ServerReply.REJECTED);
			queueReply(send);
		} else if (recv.getCommand() == Command.logout) {
			server.logoff(username);
			try {
				server.startNewTimer(new SocketParams(mySocket, received, sent));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(recv.getCommand() == Command.readlog){
			try {
				server.readlog(username);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (recv.getCommand() == Command.join)
			server.joinGroup(this, recv.getGname());
		else if (recv.getCommand() == Command.leave)
			server.leaveGroup(this, recv.getGname());
		else if (recv.getCommand() == Command.send) {
			send(recv.getDest(), recv.getMessage(), recv.getSQN());
		}
		else if (recv.getCommand() == Command.adduser){
			ServerReply success = server.addUser(recv.getUsername(), recv.getPassword());
			TransportObject sendObject = new TransportObject(Command.adduser,success);
			queueReply(sendObject);
		}
	}
}
