package org.poc.grpc.eventhandler.client;

import java.io.IOException;

public class ApplicationClient {
	
	public static void main(String[] args) throws InterruptedException, IOException {
		EventHandlerClient client = new EventHandlerClient("localhost", 7777);
		
		//client.streamEvent();
		
		
		
		for (int i = 0; i<=300; i++) {
			client.sendEvent();
			if(args != null && args.length > 0 && args[0].equals("wait")) {
				System.in.read();
			}
		}
		
		client.shutdown();
	}

}
