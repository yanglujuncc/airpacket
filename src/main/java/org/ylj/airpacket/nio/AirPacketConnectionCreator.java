package org.ylj.airpacket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class AirPacketConnectionCreator {

	private static final Logger logger = Logger.getLogger(AirPacketConnectionCreator.class);

	public static Future<AirPacketConnection> createClient(String host, int port, EventReactor eventReactor) throws IOException {

		InetSocketAddress remoteInetAddr = new InetSocketAddress(host, port);
		SocketChannel pendingSocketChannel = SocketChannel.open();
		pendingSocketChannel.configureBlocking(false);

		PendingConnection pendingConnection = new PendingConnection(eventReactor);
		
		pendingConnection.pendingSocketChannel = pendingSocketChannel;	
		pendingConnection.remoteInetAddr=remoteInetAddr;
	
		
		// 如果直接连接成功
		if (pendingSocketChannel.connect(remoteInetAddr)) {
			logger.info("connect success,first, local:" + pendingSocketChannel.socket().getLocalAddress() + " remote:" + pendingSocketChannel.getRemoteAddress());
			pendingConnection.success=true;	
			pendingConnection.countDownLatch = new CountDownLatch(0);	
			///pendingConnection.selectionKey =null;
			
		} else {
			pendingConnection.success=false;
			pendingConnection.countDownLatch = new CountDownLatch(1);		
			eventReactor.registerEventInterestOps(pendingSocketChannel, SelectionKey.OP_CONNECT, pendingConnection);
		}
		
		return new AirMsgClientFuture(pendingConnection);

	}
   private  static class PendingConnection implements EventHandler {
		
		CountDownLatch countDownLatch;
		InetSocketAddress remoteInetAddr;
		SocketChannel pendingSocketChannel;
		//SelectionKey selectionKey;
		boolean success;
		boolean cancel=false;
		EventReactor eventReactor=null;
		
		public PendingConnection(EventReactor eventReactor){
			this.eventReactor=eventReactor;
		}
		@Override
		public void doNotValid() {
			
			
			try {
				eventReactor.unRegisterEventInterestOps(pendingSocketChannel);
				logger.info("pendingSocketChannel not Valid,then ungister event,closing channel... ");				
				pendingSocketChannel.close();
			} catch (Throwable e) {
				logger.error("", e);
			}

			
			logger.error("connect failed, "+ " remote:" + remoteInetAddr);
			pendingSocketChannel = null;
			success = false;
			countDownLatch.countDown();
			cancel=true;
		}

		@Override
		public void doAcceptable() {

		}
		public void unRegisterEvent(){
			eventReactor.unRegisterEventInterestOps(pendingSocketChannel);
			
		}
		@Override
		public void doConnectable() {
			
			try {
				if (pendingSocketChannel.finishConnect()) {
					
					cancel=true;
				//	selectionKey=null;
					success = true;
					
						
					logger.info("connect success, local:" + pendingSocketChannel.socket().getLocalAddress() + " remote:" + remoteInetAddr);
				}else{
					cancel=true;
					//selectionKey=null;
					pendingSocketChannel = null;
					success = false;
				}
			} catch (IOException e) {
	
				logger.error("connect failed, "+ " remote:" + remoteInetAddr);
				cancel=true;
			//	selectionKey=null;
				pendingSocketChannel = null;
				success = false;
			}
	
			eventReactor.unRegisterEventInterestOps(pendingSocketChannel);
			countDownLatch.countDown();

		}

		@Override
		public void doReadable() {
			
		}

		@Override
		public void doWritable() {
			

		}

	
	}
	
   public static class AirMsgClientFuture implements Future<AirPacketConnection>{
		
	    PendingConnection pendingConnection;
		boolean isCancel=false;
		public AirMsgClientFuture(PendingConnection pendingConnection){
			this.pendingConnection=pendingConnection;
		}
		
		@Override
		public boolean cancel(boolean arg0) {
			logger.info("cancel()");
			pendingConnection.unRegisterEvent();
			isCancel=true;
			return true;
		}

		@Override
		public AirPacketConnection get() throws InterruptedException, ExecutionException {
			logger.info("get()");
			
			 pendingConnection.countDownLatch.await();
			 
			 if(pendingConnection.success){
				 return new AirPacketConnection(pendingConnection.pendingSocketChannel);
			 }else{
				 return null;
			 }
			 
		}

		@Override
		public AirPacketConnection get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
			logger.info("get()");
			
			pendingConnection.countDownLatch.await(arg0,arg1);
			 
			 if(pendingConnection.success){
				 
				 return new AirPacketConnection(pendingConnection.pendingSocketChannel);
			 }else{
				 return null;
			 }
		}

		@Override
		public boolean isCancelled() {
			logger.info("isCancelled()");
			return 	isCancel;
		}

		@Override
		public boolean isDone() {
			logger.info("isDone()");
			return pendingConnection.success;
		}
		
	}
   public static void main(String[] args) throws ClosedChannelException, IOException, InterruptedException, ExecutionException {

		DOMConfigurator.configure("conf/log4j.xml");

		EventReactor eventReactor = new EventReactor();
		eventReactor.start();
		
		String host="localhost";
		int port=8080;
		
		Future<AirPacketConnection> airMsgClientFuture =AirPacketConnectionCreator.createClient(host, port, eventReactor);
		AirPacketConnection client=airMsgClientFuture.get();
		System.out.println("get client success ,"+client);
	}

	
}
