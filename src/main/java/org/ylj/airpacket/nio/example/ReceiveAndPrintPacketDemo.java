package org.ylj.airpacket.nio.example;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.ylj.airpacket.nio.AirPacketConnection;
import org.ylj.airpacket.nio.AirPacketConnectionAcceptor;
import org.ylj.airpacket.nio.CallbackConnectionBroken;
import org.ylj.airpacket.nio.CallbackPacketReceived;
import org.ylj.airpacket.nio.EventReactor;
import org.ylj.airpacket.nio.AirPacketConnectionAcceptor.TimerTaskTest;
import org.ylj.airpacket.protocol.Packet;
import org.ylj.airpacket.utils.ByteUtils;

public class ReceiveAndPrintPacketDemo {

	private static final Logger logger = Logger.getLogger(ReceiveAndPrintPacketDemo.class);

	public static class PrintPacketCallback implements CallbackPacketReceived {

		@Override
		public void handleReceivedPacket(Packet packet) {

			try {
				
				logger.info(packet.header.toString()+" "+ ByteUtils.bytesToHex(packet.body)+" "+new String(packet.body,"utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	public static class SimpleCallbackConnectionBroken implements CallbackConnectionBroken{

		@Override
		public void doChannelBroken() {
			// TODO Auto-generated method stub
			logger.info(" doChannelBroken,do nothing");
		}
		
	}

	public static void main(String[] args) throws Exception {

		DOMConfigurator.configure("conf/log4j.xml");

		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
				new ThreadPoolExecutor.CallerRunsPolicy());

		EventReactor eventReactor = new EventReactor(threadPool);
		eventReactor.start("event");
		

		//EventReactor readEventReactor = new EventReactor(threadPool);
		//readEventReactor.start("read");

		AirPacketConnectionAcceptor listenChannel = new AirPacketConnectionAcceptor(eventReactor);
		listenChannel.listen(8080);
		Timer timer = new Timer();
		timer.schedule(new TimerTaskTest(listenChannel), 1000, 2000);

		List<AirPacketConnection> acceptConnections = new LinkedList<AirPacketConnection>();
		while (true) {
			AirPacketConnection airPacketConnection = listenChannel.nextAcceptedConnection();
			airPacketConnection.setEventReactor(eventReactor);
			airPacketConnection.setReceivedCallback(new PrintPacketCallback());
			airPacketConnection.setBrokenCallback(new SimpleCallbackConnectionBroken());
			
			acceptConnections.add(airPacketConnection);
		}

	}
}
