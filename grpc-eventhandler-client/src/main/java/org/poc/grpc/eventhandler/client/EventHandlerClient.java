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

import io.grpc.Status;
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

		try {
			response = eventStub.sendEvent(event);
		} catch (Exception e) {
			logger.info(e.toString());
			Status status = Status.fromThrowable(e);
			logger.info("Status Code: " + status.getCode() + ", Cause: " + status.getCause() + ", Message: " + status.getDescription());
			return;
		}

		logger.info("Response for: " + eventId + " received with Id:" + response.getEventId());

		return;

	}
	
	public void streamEvent() throws InterruptedException, IOException {
		
		Map<Integer, Event> eventsMap = new HashMap<Integer, Event>();
		for (int i = 1; i<=100; i++) {
			int eventId = random.nextInt(Integer.MAX_VALUE);
			String serial = UUID.randomUUID().toString();
			eventsMap.put(eventId, Event.newBuilder().setId(eventId).setName("Serial").setAttribute(serial).build());
		}
		
		logger.info("Sending events: " + eventsMap);
		
		StreamObserver<Event> requestObserver = streamStub.streamEvent(new StreamObserver<Response>() {
			
			@Override
			public void onNext(Response response) {
				logger.info("Response Id:" + response.getEventId());
				eventsMap.remove(response.getEventId());
			}
			
			@Override
			public void onError(Throwable e) {
				Status status = Status.fromThrowable(e);
				logger.info("Status Code: " + status.getCode() + ", Cause: " + status.getCause() + ", Message: " + status.getDescription());

				
			}
			
			@Override
			public void onCompleted() {
				logger.info("Transfer Completed. Remaining events: " + eventsMap );
			}
		});
		
		int count = 0;
		
		Map<Integer, Event> sendingEventsMap = new HashMap<Integer, Event>();
		sendingEventsMap.putAll(eventsMap);
		
		for (Map.Entry<Integer, Event> entry : sendingEventsMap.entrySet()) {
			if (channel.isTerminated())
				logger.info("Connection Terminated");
			
			if (channel.isShutdown())
				logger.info("Connection Shutdown");
			logger.info("Sending event: " + entry.getValue());
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
