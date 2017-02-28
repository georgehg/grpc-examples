package org.ghgs.grpc.eventhandler.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.ghgs.grpc.eventhandler.api.Event;
import org.ghgs.grpc.eventhandler.api.EventHandlerGrpc;
import org.ghgs.grpc.eventhandler.api.EventHandlerGrpc.EventHandlerBlockingStub;
import org.ghgs.grpc.eventhandler.api.EventHandlerGrpc.EventHandlerStub;
import org.ghgs.grpc.eventhandler.api.Response;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub;
import io.grpc.stub.StreamObserver;

public class EventHandlerClient {

	private static final Logger log = Logger.getLogger(EventHandlerClient.class.getName());

	private final ManagedChannel channel;
	private final EventHandlerBlockingStub eventStub;
	private final EventHandlerStub streamStub;
	private final HealthBlockingStub healthStub;
	
	private final Random random = new Random();

	public EventHandlerClient(String hostName, int port) {
		this.channel = ManagedChannelBuilder.forAddress(hostName, port).usePlaintext(true).build();
		this.eventStub = EventHandlerGrpc.newBlockingStub(channel);
		this.streamStub = EventHandlerGrpc.newStub(channel);
		this.healthStub = HealthGrpc.newBlockingStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}
	
	public Boolean checkServerHealth() {
		log.info("Checking Server Health Status");
		HealthCheckResponse response = healthStub.check(HealthCheckRequest.newBuilder().setService("EventHandler").build());
		log.info("Server Health Status is " + response.getStatus());
		return response.getStatus().equals(ServingStatus.SERVING);
	}

	public void sendEvent() {

		int eventId = random.nextInt(Integer.MAX_VALUE);
		String serial = UUID.randomUUID().toString();

		log.info("Sending event: " + eventId + " with Serial: " + serial);

		Event event = Event.newBuilder().setId(eventId).setName("Serial").setAttribute(serial).build();

		Response response;

		try {
			response = eventStub.sendEvent(event);
		} catch (Exception e) {
			log.info(e.toString());
			Status status = Status.fromThrowable(e);
			log.info("Status Code: " + status.getCode() + ", Cause: " + status.getCause() + ", Message: " + status.getDescription());
			return;
		}

		log.info("Response for: " + eventId + " received with Id:" + response.getEventId());

		return;

	}
	
	public void streamEvent() throws InterruptedException, IOException {
		
		Map<Integer, Event> eventsMap = new ConcurrentHashMap<Integer, Event>();
		for (int i = 1; i<=100; i++) {
			int eventId = random.nextInt(Integer.MAX_VALUE);
			String serial = UUID.randomUUID().toString();
			eventsMap.put(eventId, Event.newBuilder().setId(eventId).setName("Serial").setAttribute(serial).build());
		}
		
		//log.info("Sending events: " + eventsMap);
		
		StreamObserver<Event> requestObserver = streamStub.streamEvent(new StreamObserver<Response>() {
			
			@Override
			public void onNext(Response response) {
				log.info("Response Id:" + response.getEventId());
				eventsMap.remove(response.getEventId());
			}
			
			@Override
			public void onError(Throwable e) {
				Status status = Status.fromThrowable(e);
				log.info("Status Code: " + status.getCode() + ", Cause: " + status.getCause() + ", Message: " + status.getDescription());
			}
			
			@Override
			public void onCompleted() {
				log.info("Transfer Completed. Remaining events: " + eventsMap );
			}
		});
		
		int count = 0;
		
		for (Map.Entry<Integer, Event> entry : eventsMap.entrySet()) {
			
			if (channel.isTerminated())
				log.info("Connection Terminated");
			
			if (channel.isShutdown())
				log.info("Connection Shutdown");
			
			log.info("Sending event: " + entry.getValue());
			Thread.sleep(random.nextInt(500));
			requestObserver.onNext(entry.getValue());
			count++;
			if (count == 10) {
				//System.in.read();
				count = 0;
			}
		}
		
		Thread.sleep(3000);
		
		requestObserver.onCompleted();
		
		return;
	}
}
