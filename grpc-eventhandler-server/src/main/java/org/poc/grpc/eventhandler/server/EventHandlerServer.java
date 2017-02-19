package org.poc.grpc.eventhandler.server;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class EventHandlerServer {
	
	private static final Logger logger = Logger.getLogger(EventHandlerServer.class.getName());
	
	private final int port;
	
	private final Server server;
	
	public EventHandlerServer(int port) {
		this.port = port;
		this.server = ServerBuilder.forPort(port).addService(new EventHandlerService()).build();
	}
	
	public void start() throws IOException {
		server.start();
		logger.info("Server started, listening on port: " + port);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		
			@Override
			public void run() {
				System.err.println("*** shutting down gRPC server since JVM is shutting down"); 
				EventHandlerServer.this.stop(); 
				System.err.println("*** server shut down"); 
			}
		});
	}

	public void stop() {
		if (server != null) {
			server.shutdown();
		}
	}
	
	public void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}
}