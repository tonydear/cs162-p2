package edu.berkeley.cs.cs162;

import java.io.Serializable;

public class TransportObject implements Serializable {
	private Command command;
	private String username;
	private String gname;
	private String dest;
	private int sqn;
	private String msg;
	private int time;
	private ServerReply reply;
	
	//Default; client logout, disconnect
	public TransportObject(Command cmd) {
		command = cmd;
		username = null;
		gname = null;
		dest = null;
		sqn = 0;
		msg = null;
		time = 0;
		reply = null;
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
	
	//Client sleep
	public TransportObject(Command cmd, int time) {
		this(cmd);
		this.time = time;
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
	
	//Server send
	public TransportObject(Command cmd, int sqn, ServerReply reply) {
		this(cmd);
		this.sqn = sqn;
		this.reply = reply;
	}
}
