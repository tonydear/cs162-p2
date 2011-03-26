package edu.berkeley.cs.cs162;

import java.io.Serializable;

public class TransportObject implements Serializable {
	private Command command;
	private String username;
	private String gname;
	private String sender;
	private String dest;
	private int sqn;
	private String msg;
	private int time;
	private ServerReply reply;
	
	//Default
	public TransportObject() {
		command = null;
		username = null;
		gname = null;
		sender = null;
		dest = null;
		sqn = 0;
		msg = null;
		time = 0;
		reply = null;
	}
	
	//Default with cmd; client logout, disconnect
	public TransportObject(Command cmd) {
		this();
		command = cmd;
	}
	
	//Client login, join, leave
	public TransportObject(Command cmd, String in1) {
		this(cmd);
		if (cmd == Command.LOGIN)
			username = in1; 
		else 
			gname = in1; 
	}
	
	//Client send
	public TransportObject(Command cmd, String dest, int sqn, String msg) {
		this(cmd);
		this.dest = dest;
		this.sqn = sqn;
		this.msg = msg;
	}
	
	//Server disconnect, login, logout
	public TransportObject(Command cmd, ServerReply reply) {
		this(cmd);
		this.reply = reply;
	}
	
	//Server join, leave
	public TransportObject(Command cmd, String gname, ServerReply reply) {
		this(cmd);
		this.gname = gname;
		this.reply = reply;
	}
	
	//Server send, sendack fail
	public TransportObject(Command cmd, int sqn, ServerReply reply) {
		this(cmd);
		this.sqn = sqn;
		this.reply = reply;
	}
	
	//Server receive 
	public TransportObject(ServerReply reply, String sender, String dest, String msg) {
		this();
		this.reply = reply;
		this.sender = sender;
		this.dest = dest;
		this.msg = msg;
	}
	
	//Timeout
	public TransportObject(ServerReply reply) {
		this();
		this.reply = reply;
	}
}
