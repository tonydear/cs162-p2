package edu.berkeley.cs.cs162;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class ChatClient extends Thread{
	private Socket mySocket;
	private Map<String,ChatLog> logs;
	private BufferedReader commands;
	private ObjectInputStream received;
	private ObjectOutputStream sent;
	private Thread receiver;
	private boolean connected;
	private Command reply; 				//what reply from server should look like
	private volatile boolean isWaiting; //waiting for reply from server?
	
	public ChatClient(){
		mySocket = null;
		logs = new HashMap<String,ChatLog>();
		commands = new BufferedReader(new InputStreamReader(System.in));
		
		connected = false;
		isWaiting = false;
		reply = null;
		
		receiver = new Thread(){
            @Override
            public void run(){
            	while(true){
            		receive();
            	}
            }
        };
        receiver.start();
        start();
	}
	
	private void connect(String hostname, int port){
		try {
			mySocket = new Socket(hostname,port);
			received = new ObjectInputStream(mySocket.getInputStream());
			sent = new ObjectOutputStream(mySocket.getOutputStream());
			connected = true;
			output("connect OK");
		} catch (Exception e) {
			output("connect REJECTED");
			e.printStackTrace();
		}
	}
	
	private void disconnect(){
		try {
			mySocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		output("disconnect OK");
	}
	
	private void output(String o){
		System.out.append(o);
	}
	
	//TODO
	private void login(String username){
		if (!connected)
			return;
		TransportObject toSend = new TransportObject(Command.LOGIN,username);
		try {
			sent.writeObject(toSend);
			isWaiting = true;
			reply = Command.LOGIN;
			this.wait();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//TODO
	private void logout(){
		if (!connected)
			return;
	}
	
	//TODO
	private boolean join(String gname){
		TransportObject toSend = new TransportObject(Command.JOIN,gname);
		return false;
	}
	
	//TODO
	private boolean leave(String gname){
		return false;
	}
	
	//TODO
	private void send(String dest, int sqn, String msg){
		TransportObject toSend = new TransportObject(Command.SEND,dest,sqn,msg);
		try {
			sent.writeObject(toSend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void receive(){
		TransportObject recObject = null;
		try {
			recObject = (TransportObject) received.readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (recObject == null)
			return;
		Command type = recObject.getCommand();
		ServerReply servReply = recObject.getServerReply();
		if (isWaiting && type.equals(reply)) {
			if (reply.equals(Command.LOGIN)) {
				if(servReply.equals(ServerReply.OK)) {
					
				} else if(servReply.equals(ServerReply.REJECTED)) {
					
				} else if(servReply.equals(ServerReply.QUEUED)) {
					
				}
			} else if(reply.equals(Command.JOIN)){
				if(servReply.equals(ServerReply.BAD_GROUP)){
					
				}else if(servReply.equals(ServerReply.OK_JOIN)){
					
				}else if(servReply.equals(ServerReply.OK_CREATE)){
					
				}else if(servReply.equals(ServerReply.FAIL_FULL)){
					
				}
			}else if(reply.equals(Command.LEAVE)){
				
			}
		}else if(type.equals(Command.SEND)){
			
		}else if(type.equals(Command.NONE)){
			
		}
		
	}
	
	private void sleep(int time){
		this.sleep(time);
	}
	
	public Map<String,ChatLog> getLogs(){
		return logs;
	}
	
	public synchronized void processCommands() throws Exception {
		String command = null;
		try {
			command = commands.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		String[] tokens = command.split(" ");
		int args = tokens.length;
		if(tokens.length == 0)
			return;
		if(tokens[0].equals("connect")){
			if(args != 2)
				throw new Exception("invalid arguments for connect command");
			tokens = tokens[1].split(":");
			args = tokens.length;
			if(args != 2)
				throw new Exception("invalid arguments for connect command");
			String hostname = tokens[0];
			int port;
			try {
				port = Integer.parseInt(tokens[1]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			}
			connect(hostname, port);
		}
		else if(tokens[0].equals("disconnect")) {
			if(args != 1)
				throw new Exception("invalid arguments for disconnect command");
			disconnect();
		}
		else if(tokens[0].equals("login")) {
			if(args != 2)
				throw new Exception("invalid arguments for login command");
			String username = tokens[1];
			login(username);
		}
		else if(tokens[0].equals("logout")) {
			if(args != 1)
				throw new Exception("invalid arguments for logout command");
			logout();
		}
		else if(tokens[0].equals("join")) {
			if(args != 2)
				throw new Exception("invalid arguments for join command");
			String gname = tokens[1];
			join(gname);
		}
		else if(tokens[0].equals("leave")) {
			if(args != 2)
				throw new Exception("invalid arguments for leave command");
			String gname = tokens[1];
			leave(gname);
		}
		else if(tokens[0].equals("send")) {
			if(args != 4)
				throw new Exception("invalid arguments for send command");
			String dest = tokens[1];
			int sqn;
			try {
				sqn = Integer.parseInt(tokens[2]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			}
			String msg = tokens[3];
			send(dest,sqn,msg);
		}
		else if(tokens[0].equals("sleep")) {
			if(args != 2)
				throw new Exception("invalid arguments for sleep command");
			int time;
			try {
				time = Integer.parseInt(tokens[1]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			}
			sleep(time);
		}
		else {
			throw new Exception("invalid command");
		}
	}
	
	private synchronized void signalReceive(){
		
	}
	
	@Override
	public void run(){
		while(true){
			try {
				processCommands();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		ChatClient client = new ChatClient();
	}
	
}
