package org.ghgs.grpc.eventhandler.client;

import java.util.concurrent.TimeUnit;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ChannelBean {

	private final ManagedChannel channel =  ManagedChannelBuilder.forTarget("grpc-eventhandler-server:7070").usePlaintext(true).build();
	
	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}
	
	public Channel getChannel() {
		return channel;
	}
}
