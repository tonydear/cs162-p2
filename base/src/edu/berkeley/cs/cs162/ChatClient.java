package edu.berkeley.cs.cs162;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class ChatClient extends Thread{
	private Socket mySocket;
	private Map<String,ChatLog> logs;
	private BufferedReader commands;
	private ObjectInputStream received;
	private ObjectOutputStream sent;
	private Thread receiver;
	private boolean connected;
	private TransportObject reply; //what should reply from server look like
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
			// TODO Auto-generated catch block
			output("connect REJECTED");
			e.printStackTrace();
		}
	}
	
	private void disconnect(){
		try {
			mySocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		output("disconnect OK");
	}
	
	private void output(String o){
		System.out.append(o);
	}
	
	private boolean login(String username){
		if(!connected)
			return false;
		
		return false;
	}
	
	private void logout(){
		if(!connected)
			return;
	}
	
	private boolean join(String gname){
		return false;
	}
	
	private boolean leave(String gname){
		return false;
	}
	
	private void send(String dest, int sqn, String msg){
		
	}
	
	private void receive(){
		
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
			processCommands();
		}
	}
	
	public static void main(String[] args){
		ChatClient client = new ChatClient();
	}
	
}
