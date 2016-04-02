package org.ylj.airpacket.nio.bk;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import org.apache.log4j.xml.DOMConfigurator;
import org.ylj.airpacket.nio.EventReactor;

public class DemoServer {
	public static void main(String[] args) throws ClosedChannelException, IOException, InterruptedException {

		DOMConfigurator.configure("conf/log4j.xml");

		EventReactor eventReactor = new EventReactor();
		eventReactor.start();
		ChannelListener listenChannel = new ChannelListener(eventReactor);
		listenChannel.listen(8080);
		
		while(true){
			SocketChannel socketChannel=listenChannel.nextAcceptedChannel();
			ChannelTransportor channelTransportor=new ChannelTransportor(eventReactor,socketChannel);
			channelTransportor.startReceive();
		}
	
		
	}
}
