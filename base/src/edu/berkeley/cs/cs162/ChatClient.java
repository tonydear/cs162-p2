package edu.berkeley.cs.cs162;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class ChatClient extends Thread{
	private Socket mySocket;
	private Map<String,ChatLog> logs;
	private InputStream commands;
	private ObjectInputStream received;
	private ObjectOutputStream sent;
	private Thread receiver;
	private boolean connected;
	private Message reply; //what should reply from server look like
	private volatile boolean isWaiting; //waiting for reply from server?
	
	public ChatClient(){
		mySocket = new Socket();
		logs = new HashMap<String,ChatLog>();
		try {
			received = new ObjectInputStream(mySocket.getInputStream());
			sent = new ObjectOutputStream(mySocket.getOutputStream());
			commands = System.in;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	private boolean connect(String hostname, int port){
		return false;
	}
	
	private void disconnect(){
		
	}
	
	private boolean login(String username){
		return false;
	}
	
	private void logout(){
		
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
		
	}
	
	public Map<String,ChatLog> getLogs(){
		return null;
	}
	
	public synchronized void processCommands(){
		 
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
