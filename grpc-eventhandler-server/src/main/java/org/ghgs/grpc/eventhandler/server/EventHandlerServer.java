package org.ghgs.grpc.eventhandler.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.services.HealthStatusManager;

@ConfigurationProperties(prefix="server")
public class EventHandlerServer {
	
	private static final Logger log = LoggerFactory.getLogger(EventHandlerServer.class.getName());
	
	private int port;
	
	private final Server server;
	
	public EventHandlerServer() throws IOException, InterruptedException {
		
		HealthStatusManager healthStatusService = new HealthStatusManager();
		healthStatusService.setStatus("EventHandler", ServingStatus.SERVING);
		
		this.server = ServerBuilder.forPort(7070)
								    .addService(new EventHandlerService().bindService())
								    .addService(healthStatusService.getHealthService())
								    .build();
		
		start();
		blockUntilShutdown();
	}
	
	public Server getServer() {
		return server;
	}
	
	public void start() throws IOException {
		server.start();
		log.info("Server started, listening on port: {}", port);
		
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
