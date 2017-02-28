package org.ghgs.grpc.eventhandler.server;

import java.io.IOException;

public class ApplicationServer {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		EventHandlerServer server = new EventHandlerServer(7777);
		
		server.start();
		
		server.blockUntilShutdown();
	}

}
