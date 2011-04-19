package edu.berkeley.cs.cs162;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BenchmarkSender extends AbstractChatClient {
	private Map<Integer,Long> benchmarkTimes = new HashMap<Integer,Long>();
	private Lock sendLock = new ReentrantLock();
	
	@Override
	protected void send(String dest, int sqn, String msg){
		sendLock.lock();
		super.send(dest,sqn,msg);
		benchmarkTimes.put(sqn,System.currentTimeMillis());
		sendLock.unlock();
	}
	
	private void sendRTT(double rtt){
		sendLock.lock();
		TransportObject rttObject = new TransportObject(Command.rtt,rtt);
		try {
			sent.writeObject(rttObject);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Sending rtt");
		sendLock.unlock();
	}
	
	@Override
	protected void benchmark(TransportObject object){
		long sentTime = benchmarkTimes.get(object.getSQN());
		benchmarkTimes.remove(object.getSQN());
		long receivedTime = System.currentTimeMillis();
		double rrt = (receivedTime - sentTime)/1000;
		sendRTT(rrt);
	}
	
	public static void main(String[] args) throws UnknownHostException{
		new BenchmarkSender();
	}
	
}
