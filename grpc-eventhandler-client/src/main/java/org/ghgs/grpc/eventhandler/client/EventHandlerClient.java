package org.ghgs.grpc.eventhandler.client;

import java.io.IOException;

import javax.annotation.PostConstruct;

import io.grpc.health.v1.HealthCheckResponse.ServingStatus;

public class EventHandlerClient {
	
	@PostConstruct
	public void start() throws InterruptedException, IOException{
		
		ChannelBean channel = new ChannelBean();
		
		EventHandlerService eventClient = new EventHandlerService(channel.getChannel());
		
		Boolean serverStatus = false;
		do {
			serverStatus = eventClient.checkServerHealth();
			Thread.sleep(3000);
		} while (!serverStatus);

		eventClient.streamEvent();
		
		System.in.read();
		
		for (int i = 0; i<=300; i++) {
			eventClient.sendEvent();
			//if(args != null && args.length > 0 && args[0].equals("wait")) {
			//	System.in.read();
			//}
		}
		
		channel.shutdown();
	}

}
