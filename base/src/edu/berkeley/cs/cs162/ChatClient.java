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
		if(tokens.length == 0)
			return;
		if(tokens[0].equals("connect")){
			
		}
		else if(tokens[0].equals("disconnect")) {
			
		}
		else if(tokens[0].equals("login")) {
			
		}
		else if(tokens[0].equals("logout")) {
			
		}
		else if(tokens[0].equals("join")) {
			
		}
		else if(tokens[0].equals("leave")) {
			
		}
		else if(tokens[0].equals("send")) {
			
		}
		else if(tokens[0].equals("sleep")) {
			
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
