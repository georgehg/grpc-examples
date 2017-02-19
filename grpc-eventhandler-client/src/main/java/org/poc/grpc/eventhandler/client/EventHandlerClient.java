package org.poc.grpc.eventhandler.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.poc.grpc.eventhandler.api.Event;
import org.poc.grpc.eventhandler.api.EventHandlerGrpc;
import org.poc.grpc.eventhandler.api.EventHandlerGrpc.EventHandlerBlockingStub;
import org.poc.grpc.eventhandler.api.EventHandlerGrpc.EventHandlerStub;
import org.poc.grpc.eventhandler.api.Response;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class EventHandlerClient {

	private static final Logger logger = Logger.getLogger(EventHandlerClient.class.getName());

	private final ManagedChannel channel;
	private final EventHandlerBlockingStub eventStub;
	private final EventHandlerStub streamStub;
	private final Random random = new Random();

	public EventHandlerClient(String hostName, int port) {
		this.channel = ManagedChannelBuilder.forAddress(hostName, port).usePlaintext(true).build();
		this.eventStub = EventHandlerGrpc.newBlockingStub(channel);
		this.streamStub = EventHandlerGrpc.newStub(channel);
		
		
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	public void sendEvent() {

		int eventId = random.nextInt(Integer.MAX_VALUE);
		String serial = UUID.randomUUID().toString();

		logger.info("Sending event: " + eventId + " with Serial: " + serial);

		Event event = Event.newBuilder().setId(eventId).setName("Serial").setAttribute(serial).build();

		Response response;

		response = eventStub.sendEvent(event);

		logger.info("Response for: " + eventId + " received with Id:" + response.getEventId() + " and status: " + response.getStatus());

		return;

	}
	
	public void streamEvent() throws InterruptedException, IOException {
		
		Map<Integer, Event> eventsMap = new HashMap<Integer, Event>();
		for (int i = 1; i<=100; i++) {
			int eventId = random.nextInt(Integer.MAX_VALUE);
			String serial = UUID.randomUUID().toString();
			eventsMap.put(eventId, Event.newBuilder().setId(eventId).setName("Serial").setAttribute(serial).build());
		}
		
		StreamObserver<Event> responseObserver = streamStub.streamEvent(new StreamObserver<Response>() {
			
			@Override
			public void onNext(Response response) {
				logger.info("Response Id:" + response.getEventId() + " and status: " + response.getStatus());
				logger.info("Found event: " + eventsMap.get(response.getEventId()));
			}
			
			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleted() {
				logger.info("Transfer Completed");
				
			}
		});
		
		int count = 0;
		for (Map.Entry<Integer, Event> entry : eventsMap.entrySet()) {
			logger.info("Sending event: " + entry.getValue());
			Thread.sleep(random.nextInt(500));
			responseObserver.onNext(entry.getValue());
			count++;
			if (count == 10) {
				System.in.read();
				count = 0;
			}
		}
	
		Thread.sleep(3000);
		
		responseObserver.onCompleted();
		
		return;
	}
}
