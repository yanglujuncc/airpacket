package org.ylj.airpacket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.ylj.airpacket.nio.EventHandler;
import org.ylj.airpacket.nio.EventReactor;

public class AirPacketConnectionAcceptor {

	private static final Logger logger = Logger.getLogger(AirPacketConnectionAcceptor.class);

	EventReactor eventReactor;

	int port;
	ServerSocketChannel servChannel;
	//SelectionKey selectionKey;
	ChannelListenerEventHandler eventHandler;
	LinkedBlockingQueue<SocketChannel> completeChannles = new LinkedBlockingQueue<SocketChannel>();

	public AirPacketConnectionAcceptor(EventReactor eventReactor) {
		this.eventReactor = eventReactor;
		this.eventHandler=new ChannelListenerEventHandler();	
	
	}

	public void listen(int port) throws IOException {
		this.port = port;
		this.servChannel = ServerSocketChannel.open();
		this.servChannel.configureBlocking(false);
		this.servChannel.socket().bind(new InetSocketAddress(port), 1024);	
	
		this.eventReactor.registerEventInterestOps(servChannel, SelectionKey.OP_ACCEPT, eventHandler);
		logger.info("listen port:"+port+",register to selector.");
	}

	/**
	 * blocking
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public AirPacketConnection nextAcceptedConnection() throws InterruptedException {

		SocketChannel socketChannel=completeChannles.take();
		 
		 
		return  new AirPacketConnection(socketChannel);
	}

	public class ChannelListenerEventHandler implements EventHandler {

		@Override
		public void doNotValid() {

		

			try {
				eventReactor.unRegisterEventInterestOps(servChannel);
				logger.info("servChannel not Valid,then ungister event,closing channel... ");				
				servChannel.close();
			} catch (Throwable e) {
				logger.error("", e);
			}

			
		}

		@Override
		public void doAcceptable() {

			try {
							
				SocketChannel sc = servChannel.accept();
				sc.configureBlocking(false);
				completeChannles.add(sc);
				logger.info("accept success, local:" + sc.socket().getLocalAddress() + " remote:" + sc.getRemoteAddress());
			} catch (IOException e) {
				// e.printStackTrace();
				logger.error("accept failed. ", e);
			}

		}

		@Override
		public void doConnectable() {
			// TODO Auto-generated method stub

		}

		@Override
		public void doReadable() {
			// TODO Auto-generated method stub

		}

		@Override
		public void doWritable() {
			// TODO Auto-generated method stub

		}


	}

	public void printStatus() throws IOException{
	//	System.out.println("completeChannles:"+completeChannles.size());
		for(SocketChannel completeChannle:completeChannles){
			System.out.println("isOpen:"+completeChannle.isOpen()+" "+completeChannle.getLocalAddress()+" "+completeChannle.getRemoteAddress());
		}
	}
	public static class TimerTaskTest extends java.util.TimerTask{  
		AirPacketConnectionAcceptor listenChannel;
		public TimerTaskTest(AirPacketConnectionAcceptor listenChannel){
			this.listenChannel=listenChannel;
		}
		@Override  
		public void run() {  
	  
			try {
				listenChannel.printStatus();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}  
	} 


	public static void main(String[] args) throws ClosedChannelException, IOException, InterruptedException {

		DOMConfigurator.configure("conf/log4j.xml");

		EventReactor eventReactor = new EventReactor();
		eventReactor.start();
		AirPacketConnectionAcceptor listenChannel = new AirPacketConnectionAcceptor(eventReactor);
		listenChannel.listen(8080);
		
		Timer timer = new Timer();  
   		timer.schedule(new TimerTaskTest(listenChannel), 1000, 2000);  
	}
}
