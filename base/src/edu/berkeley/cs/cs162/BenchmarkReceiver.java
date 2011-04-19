package edu.berkeley.cs.cs162;

import java.io.EOFException;
import java.net.SocketException;

public class BenchmarkReceiver extends ChatClient {
	@Override
	protected void benchmark(TransportObject recObject) {
		send(recObject.getSender(),recObject.getSQN(),recObject.getMessage() + "ghost");
	}
}
