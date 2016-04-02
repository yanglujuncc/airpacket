package org.ylj.airpacket.nio.bk;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.ylj.airpacket.nio.EventHandler;
import org.ylj.airpacket.nio.EventReactor;

public class ChannelConnector {

	private static final Logger logger = Logger.getLogger(ChannelConnector.class);

	// public String host;
	// public int port;
	// public InetSocketAddress inetAddr;

	EventReactor eventReactor;
	//ChannelConnectorEventHandler eventHandler;
	
	LinkedBlockingQueue<SocketChannel> completeChannles=new LinkedBlockingQueue<SocketChannel>();

	public ChannelConnector(EventReactor eventReactor) {
		this.eventReactor = eventReactor;
	}
	
	/**
	 * blocking
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public SocketChannel nextConnectedChannel() throws InterruptedException{

		return completeChannles.take();
	}
	
	public void connect(String host, int port) throws IOException {

		InetSocketAddress inetAddr = new InetSocketAddress(host, port);
		SocketChannel	pendingSocketChannel = SocketChannel.open();
		pendingSocketChannel.configureBlocking(false);

		// 如果直接连接成功，则注册到多路复用器上，发送请求消息，读应答
		if (pendingSocketChannel.connect(inetAddr)) {
			logger.info("connect success, local:" + pendingSocketChannel.socket().getLocalAddress() + " remote:" + pendingSocketChannel.getRemoteAddress());
			completeChannles.add(pendingSocketChannel);
		} else {
			
			ChannelConnectorEventHandler eventHandler=new ChannelConnectorEventHandler();
			eventHandler.pendingSocketChannel=pendingSocketChannel;
			eventHandler.pendingSelectionKey = eventReactor.registerEvent(pendingSocketChannel, SelectionKey.OP_CONNECT, eventHandler);
		}

	}

	public class ChannelConnectorEventHandler  implements EventHandler{
		
		SocketChannel pendingSocketChannel;
		SelectionKey pendingSelectionKey;
		public ChannelConnectorEventHandler(){
			
		}
		public ChannelConnectorEventHandler(SocketChannel pendingSocketChannel,SelectionKey pendingSelectionKey){
			this.pendingSocketChannel=pendingSocketChannel;
			this.pendingSelectionKey=pendingSelectionKey;
		}
		@Override
		public void doNotValid() {
			logger.info("pending socketChannel not Valid");
			try {
				pendingSocketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("", e);
			}
			logger.info("pending socketChannel closed.");

		}

		
		@Override
		public void doAcceptable() {
			// TODO Auto-generated method stub

		}

		@Override
		public void doConnectable() {

			try {
				if (pendingSocketChannel.finishConnect()) {
					
					pendingSelectionKey.cancel();

					try {
						logger.info("connect success, local:" + pendingSocketChannel.socket().getLocalAddress() + " remote:" + pendingSocketChannel.getRemoteAddress());
						completeChannles.add(pendingSocketChannel);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			} catch (IOException e) {
				logger.error("", e);
				try {
					pendingSocketChannel.close();
				} catch (IOException e1) {
				}
				logger.info("socketChannel closed. ");
			}

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
	
	public static void main(String[] args) throws ClosedChannelException, IOException, InterruptedException {

		DOMConfigurator.configure("conf/log4j.xml");

		EventReactor eventReactor = new EventReactor();
		eventReactor.start();
		ChannelConnector channelConnector = new ChannelConnector(eventReactor);
		channelConnector.connect("localhost", 8080);
		SocketChannel socketChannel=channelConnector.nextConnectedChannel();
	}
}
