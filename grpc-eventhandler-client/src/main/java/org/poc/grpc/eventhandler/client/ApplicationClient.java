package org.poc.grpc.eventhandler.client;

import java.io.IOException;

public class ApplicationClient {
	
	public static void main(String[] args) throws InterruptedException, IOException {
		EventHandlerClient client = new EventHandlerClient("localhost", 7777);
		
		client.streamEvent();
		
		/*for (int i = 0; i<=10; i++) {
			client.sendEvent();
			System.in.read();
		}*/
		
		client.shutdown();
	}

}
