package org.ylj.airpacket.nio.bk;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.ylj.airpacket.nio.EventHandler;
import org.ylj.airpacket.nio.EventReactor;

public class ChannelTransportor {

	private static final Logger logger = Logger.getLogger(ChannelTransportor.class);

	public SocketChannel socketChannel;
	public SelectionKey selectionKey;

	EventReactor eventReactor;

	public ConcurrentLinkedQueue<ByteBuffer> readedQueue = new ConcurrentLinkedQueue<ByteBuffer>();
	Lock writeLock = new ReentrantLock();
	public ConcurrentLinkedQueue<ByteBuffer> toWriteQueue = new ConcurrentLinkedQueue<ByteBuffer>();

	private long totalReadBytes = 0;
	private long totalWriteBytes = 0;

	private long totalReadPacket = 0;
	private long totalWritePacket = 0;

	private int nextReadPacketNo = 0;
	private int nextWritePacketNo = 0;

	ChannelTransportorEventHandler eventHandler ;
	
	public ChannelTransportor(EventReactor eventReactor,SocketChannel socketChannel) {
		this.eventReactor=eventReactor;
		this.socketChannel=socketChannel;
		this.eventHandler=new ChannelTransportorEventHandler();
	}

	public void addWrite(ByteBuffer toWriteByteBuffer) throws IOException {
		writeLock.lock();
		try {
			toWriteQueue.add(toWriteByteBuffer);
			addEventInterestOps(SelectionKey.OP_WRITE);
		} finally {
			writeLock.unlock();
		}

	}
	public void startReceive() throws IOException {
		
		addEventInterestOps(SelectionKey.OP_READ);
	}
	
	private void addEventInterestOps(int newInterestOp) throws IOException{
		
		if(selectionKey==null){
			selectionKey=eventReactor.registerEvent(socketChannel,newInterestOp, eventHandler);
		}else{
			int existInterestOp=selectionKey.interestOps();
			int mergedInterestOp=newInterestOp&existInterestOp;
			if(mergedInterestOp!=existInterestOp){
				selectionKey.interestOps(mergedInterestOp);
			}
		}
		
	}
	
	private void doWriteAll() throws IOException {

		while (toWriteQueue.size() > 0) {
			ByteBuffer toWriteBuffer = toWriteQueue.peek();

			int writen = socketChannel.write(toWriteBuffer); // just write to
																// the socket
																// buffer,
			totalWriteBytes += writen;
			logger.info("writen:" + writen + " totalWriten:" + totalWriteBytes);
			if (!toWriteBuffer.hasRemaining()) {
				toWriteQueue.poll();
				logger.info(" write toWriteBuffer success , toWriteQueue:" + toWriteQueue.size());
				// remove head
			} else {
				logger.info("not send all data,stall need write , remaining:" + toWriteBuffer.remaining() + ", toWriteQueue:" + toWriteQueue.size());
				break;
			}

		}

		writeLock.lock();
		try {
			if (toWriteQueue.size() == 0) {
				// no write task any more;
				selectionKey.interestOps(SelectionKey.OP_READ);
			}

		} finally {
			writeLock.unlock();

		}

		logger.info(" key.isWritable() ");
		// doWrite(sc);
	}

	private void doReadAll() throws IOException {

		while (true) {

			int BufferSize = 1024;
			ByteBuffer readBuffer = ByteBuffer.allocate(BufferSize);
			int readBytes = socketChannel.read(readBuffer);

			if (readBytes == BufferSize) {
				readBuffer.flip();
				readedQueue.add(readBuffer);
				// may have more data
				continue;
			}

			// no more data

			if (readBytes > 0) {
				readBuffer.flip();
				readedQueue.add(readBuffer);
			} else if (readBytes < 0) {
				// 对端链路关闭
				selectionKey.cancel();
				socketChannel.close();
			} else
				; // 读到0字节，忽略

			break;
		}
	}

	public class ChannelTransportorEventHandler implements EventHandler {

		@Override
		public void doNotValid() {
			logger.info("socketChannel not Valid");
			try {
				socketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("", e);
			}
			logger.info("socketChannel closed.");

		}

		@Override
		public void doAcceptable() {
			// TODO Auto-generated method stub

		}

		@Override
		public void doConnectable() {

		}

		@Override
		public void doReadable() {
			try {
				doReadAll();
			} catch (IOException e) {

			}
		}

		@Override
		public void doWritable() {
			try {
				doWriteAll();
			} catch (IOException e) {

			}

		}

		
	}

}
