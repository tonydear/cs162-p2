package edu.berkeley.cs.cs162;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketParams {
	private Socket mySocket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	public SocketParams(Socket mySocket, ObjectInputStream inputStream,
			ObjectOutputStream outputStream) {
		super();
		this.mySocket = mySocket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}
	public Socket getMySocket() {
		return mySocket;
	}
	public void setMySocket(Socket mySocket) {
		this.mySocket = mySocket;
	}
	public ObjectInputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(ObjectInputStream inputStream) {
		this.inputStream = inputStream;
	}
	public ObjectOutputStream getOutputStream() {
		return outputStream;
	}
	public void setOutputStream(ObjectOutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
}
