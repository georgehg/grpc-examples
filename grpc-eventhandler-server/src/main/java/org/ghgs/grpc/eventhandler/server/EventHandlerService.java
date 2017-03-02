package org.ghgs.grpc.eventhandler.server;


import org.ghgs.grpc.eventhandler.api.Event;
import org.ghgs.grpc.eventhandler.api.EventHandlerGrpc;
import org.ghgs.grpc.eventhandler.api.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;

public class EventHandlerService extends EventHandlerGrpc.EventHandlerImplBase {
	
	private static final Logger log = LoggerFactory.getLogger(EventHandlerService.class.getName());
	
	@Override
	public void sendEvent(Event request, StreamObserver<Response> responseObserver) {
		
		log.info("Received event: {}", request);
		/*try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		responseObserver.onNext(answerEvent(request));		
		//responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Could not Insert Event on DB").asException() );
		responseObserver.onCompleted();
	}
	
	@Override
	public StreamObserver<Event> streamEvent(StreamObserver<Response> responseObserver) {
		
		return new StreamObserver<Event>() {
			
			Long eventsCount = 0L;
			
			@Override
			public void onNext(Event request) {
				log.info("Received request: {}", request);
				eventsCount++;
				responseObserver.onNext(answerEvent(request));
				//responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Could not Insert Event on DB").asException() );
			}
			
			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onCompleted() {
				
				log.info("Completing client with {} events received", eventsCount);
				responseObserver.onCompleted();
				
			}
		};
		
	}
	
	private Response answerEvent(Event request){
		Response response = Response.newBuilder().setEventId(request.getId()).build();
		return response;
	}

}
