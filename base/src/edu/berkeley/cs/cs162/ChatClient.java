package edu.berkeley.cs.cs162;

import java.net.UnknownHostException;

public class ChatClient extends AbstractChatClient {

	public static void main(String[] args) throws UnknownHostException{
		new ChatClient();
	}
	
	@Override
	protected void benchmark(TransportObject recObject) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void send(String dest, int sqn, String msg){
		if(!connected || !isLoggedIn)
			return;
		TransportObject toSend = new TransportObject(Command.send,dest,sqn,msg);
		try {
			isWaiting = true;
			reply = Command.send;
			sent.writeObject(toSend);
			this.wait();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
