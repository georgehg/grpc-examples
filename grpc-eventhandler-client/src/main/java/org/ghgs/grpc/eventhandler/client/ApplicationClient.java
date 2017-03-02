package org.ghgs.grpc.eventhandler.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApplicationClient {
	
	@Bean
	public EventHandlerClient eventHandlerService() {
		return new EventHandlerClient();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(ApplicationClient.class, args);
    }

}
