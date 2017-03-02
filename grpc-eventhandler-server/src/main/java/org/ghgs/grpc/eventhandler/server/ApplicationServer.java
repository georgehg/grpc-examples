package org.ghgs.grpc.eventhandler.server;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.grpc.Server;

@SpringBootApplication
public class ApplicationServer {
	
	
	public static void main(String[] args) {
        SpringApplication.run(ApplicationServer.class, args);
    }
	
	@Bean
	public Server startGrpcServer() throws IOException, InterruptedException {
		return new EventHandlerServer().getServer();		
	}

}
