package org.poc.grpc.eventhandler.server;

import java.io.IOException;
import java.util.logging.Logger;

import org.poc.grpc.eventhandler.api.Event;
import org.poc.grpc.eventhandler.api.EventHandlerGrpc;
import org.poc.grpc.eventhandler.api.Response;
import org.poc.grpc.eventhandler.api.Response.Status;

import io.grpc.stub.StreamObserver;

public class EventHandlerService extends EventHandlerGrpc.EventHandlerImplBase {
	
	private static final Logger logger = Logger.getLogger(EventHandlerService.class.getName());
	
	@Override
	public void sendEvent(Event request, StreamObserver<Response> responseObserver) {
		
		logger.info("Received event: " + request);
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		responseObserver.onNext(answerEvent(request));
		//responseObserver.onError(arg0);
		responseObserver.onCompleted();
	}
	
	@Override
	public StreamObserver<Event> streamEvent(StreamObserver<Response> responseObserver) {
		
		return new StreamObserver<Event>() {
			
			@Override
			public void onNext(Event request) {
				logger.info("Received request: " + request);
				responseObserver.onNext(answerEvent(request));	
			}
			
			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleted() {
				
				logger.info("Completing client: " + responseObserver);
				responseObserver.onCompleted();
				
			}
		};
		
	}
	
	private Response answerEvent(Event request){
		Response response = Response.newBuilder().setEventId(request.getId()).setStatus(Status.OK).build();
		return response;
	}

}
