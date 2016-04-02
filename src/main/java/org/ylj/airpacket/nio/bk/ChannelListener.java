package org.ylj.airpacket.nio.bk;

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

public class ChannelListener {

	private static final Logger logger = Logger.getLogger(ChannelListener.class);

	EventReactor eventReactor;

	int port;
	ServerSocketChannel servChannel;
	//SelectionKey selectionKey;
	ChannelListenerEventHandler eventHandler;
	LinkedBlockingQueue<SocketChannel> completeChannles = new LinkedBlockingQueue<SocketChannel>();

	public ChannelListener(EventReactor eventReactor) {
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
	public SocketChannel nextAcceptedChannel() throws InterruptedException {

		return completeChannles.take();
	}

	public class ChannelListenerEventHandler implements EventHandler {

		@Override
		public void doNotValid() {

			logger.info("servChannel not Valid");
			try {
				servChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("", e);
			}
			logger.info("servChannel closed.");
		}

		@Override
		public void doAcceptable() {

			try {
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
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
		System.out.println("completeChannles:"+completeChannles.size());
		for(SocketChannel completeChannle:completeChannles){
			System.out.println("isOpen:"+completeChannle.isOpen()+" "+completeChannle.getLocalAddress()+" "+completeChannle.getRemoteAddress());
		}
	}
	public static class TimerTaskTest extends java.util.TimerTask{  
		ChannelListener listenChannel;
		public TimerTaskTest(ChannelListener listenChannel){
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
		ChannelListener listenChannel = new ChannelListener(eventReactor);
		listenChannel.listen(8080);
		
		Timer timer = new Timer();  
   		timer.schedule(new TimerTaskTest(listenChannel), 1000, 2000);  
	}
}
