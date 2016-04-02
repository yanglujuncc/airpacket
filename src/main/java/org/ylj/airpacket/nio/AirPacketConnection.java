package org.ylj.airpacket.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.ylj.airpacket.protocol.Packet;
import org.ylj.airpacket.protocol.PacketHeader;

public class AirPacketConnection {

	private static final Logger logger = Logger.getLogger(AirPacketConnection.class);

	SocketChannel socketChannel;
	EventReactor eventReactor;

	AirMsgClientEventHandler eventHandler;

	CallbackPacketReceived receivedCallback;
	CallbackConnectionBroken brokenCallback;
	/**
	 * reading packet
	 */
	Lock toReceivePacketWaitLock = new ReentrantLock();
	Lock toReceivePacketLock = new ReentrantLock();
	ToReceivePacketUnit receivingPacketUnit;

	Lock toSendPacketWaitLock = new ReentrantLock();
	Lock toSendPacketLock = new ReentrantLock();
	ConcurrentLinkedQueue<ToSendPacketUnit> toSendQueue = new ConcurrentLinkedQueue<ToSendPacketUnit>();

	private long totalReadBytes = 0;
	private long totalWriteBytes = 0;

	private long totalReadPacket = 0;
	private long totalWritePacket = 0;

	private int lastReadPacketNo = 0;
	private int lastWritePacketNo = 0;

	public AirPacketConnection(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		this.eventHandler = new AirMsgClientEventHandler();

	}

	public void setEventReactor(EventReactor eventReactor) {
		this.eventReactor = eventReactor;
	}

	public String toString() {
		return geStatus();
	}

	public String geStatus() {
		return "R/W " + totalReadBytes + "/" + totalWriteBytes + " bytes, " + "R/W " + totalReadPacket + "/" + totalWritePacket + " packets, " + "R/W "
				+ lastReadPacketNo + "/" + lastWritePacketNo + " ID ";
	}

	public void setBrokenCallback(CallbackConnectionBroken brokenCallback) {
		this.brokenCallback = brokenCallback;

	}

	public void setReceivedCallback(CallbackPacketReceived receivedCallback) throws IOException {

		this.receivedCallback = receivedCallback;

		if (eventReactor.isRegisteredEvent(socketChannel)) {
			try {
				eventReactor.setEventInterestOps(socketChannel, SelectionKey.OP_READ);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			eventReactor.registerEventInterestOps(socketChannel, SelectionKey.OP_READ, eventHandler);
		}
	}

	public void stopReceiving() throws Exception {

		eventReactor.unsetEventInterestOps(socketChannel, SelectionKey.OP_READ);
	}

	class ToSendPacketUnit {
		Packet packet;
		ByteBuffer packetBytBuffer;
		CallbackPacketSend sendCallback;

		public ToSendPacketUnit(Packet packet, CallbackPacketSend sendCallback) {
			this.packet = packet;
			this.packetBytBuffer = packet.toByteBuffer();
			this.sendCallback = sendCallback;
		}
	}

	class ToReceivePacketUnit {

		ByteBuffer headBytBuffer;
		ByteBuffer bodyBytBuffer;
		Packet packet;
		// AirMsgClientPacketSendCallback sendCallback;

		public ToReceivePacketUnit() {

			this.headBytBuffer = ByteBuffer.allocate(PacketHeader.ByteSize);
			this.headBytBuffer.clear();
			this.bodyBytBuffer = null;
			this.packet = null;
		}
	}

	public void sendPacket(Packet packet, CallbackPacketSend sendCallback) throws IOException {
		ToSendPacketUnit toSendPacketUnit = new ToSendPacketUnit(packet, sendCallback);

		toSendPacketLock.lock();
		try {
			toSendQueue.add(toSendPacketUnit);

			if (eventReactor.isRegisteredEvent(socketChannel)) {
				try {
					eventReactor.setEventInterestOps(socketChannel, SelectionKey.OP_WRITE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				eventReactor.registerEventInterestOps(socketChannel, SelectionKey.OP_WRITE, eventHandler);
			}

		} finally {
			toSendPacketLock.unlock();
		}
	}

	class AirMsgClientEventHandler implements EventHandler {

		@Override
		public void doNotValid() {
			try {
				eventReactor.unRegisterEventInterestOps(socketChannel);
				logger.info("socketChannel not Valid,then ungister event,closing channel... ");
				socketChannel.close();
			} catch (Throwable e) {
				logger.error("", e);
			}
			if (brokenCallback != null) {
				logger.info("call brokenCallback... ");
				brokenCallback.doChannelBroken();
			} else {
				logger.info("brokenCallback not set. ");
			}
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
				if (toReceivePacketWaitLock.tryLock()) {
					logger.info("got toReceivePacketWaitLock ok");
					try{
						toReceivePacketLock.lock();
						logger.info("got toReceivePacketLock ok");		
					}finally{
						toReceivePacketWaitLock.unlock();
						logger.info("release toReceivePacketWaitLock ");	
					}

					try {
						doReadAndHandleAll();
					} finally {
						toReceivePacketLock.unlock();
						logger.info("release toReceivePacketLock ");	
					}
				}else{
					logger.info("get toReceivePacketWaitLock failed");
				}

			} catch (Throwable e) {
				e.printStackTrace();
				logger.error("doReadable Exception.", e);
				doNotValid();
			}
		}

		@Override
		public void doWritable() {
			try {
				if(toSendPacketWaitLock.tryLock()){
					logger.info("got toSendPacketWaitLock ok");
					try{
						toSendPacketLock.lock();
						logger.info("got toSendPacketLock ok");	
					}finally{
						toSendPacketWaitLock.unlock();
						logger.info("release toSendPacketWaitLock ");	
					}
					
					try {
						doWriteAndHandleAll();

						if (toSendQueue.size() == 0) {
							eventReactor.unsetEventInterestOps(socketChannel, SelectionKey.OP_WRITE);
						}

					} finally {
						toSendPacketLock.unlock();
						logger.info("release toSendPacketLock ");	
					}
				}else{
					logger.info("get toSendPacketWaitLock failed");
				}
				
			} catch (Throwable e) {
				logger.error("doWritable Exception", e);
				doNotValid();
			}

		}

	}

	synchronized private void doWriteAndHandleAll() throws Exception {

		while (toSendQueue.size() > 0) {
			ToSendPacketUnit toSendPacketUnit = toSendQueue.peek();

			// just write to sokcet buffer
			int writen = socketChannel.write(toSendPacketUnit.packetBytBuffer);
			totalWriteBytes += writen;
			logger.info("writen:" + writen + " totalWriten:" + totalWriteBytes);
			if (!toSendPacketUnit.packetBytBuffer.hasRemaining()) {

				toSendQueue.poll();
				lastWritePacketNo = toSendPacketUnit.packet.header.packetNo;

				toSendPacketUnit.sendCallback.doSendSuccess();
				totalWritePacket++;
				logger.info(" write a packet success , packetNo:" + toSendPacketUnit.packet.header.packetNo + " , toSendQueue:" + toSendQueue.size());
				// remove head
			} else {
				logger.info("not send all data of a packet,stall need wait write , remaining:" + toSendPacketUnit.packetBytBuffer.remaining() + ", toSendQueue:"
						+ toSendQueue.size());
				return;
			}

		}

		// doWrite(sc);
	}

	synchronized private void doReadAndHandleAll() throws Exception {

		if (receivingPacketUnit == null) {
			receivingPacketUnit = new ToReceivePacketUnit();
		}

		while (true) {

			if (receivingPacketUnit.headBytBuffer.hasRemaining()) {
				// head read
				int readBytes = socketChannel.read(receivingPacketUnit.headBytBuffer);
				if (readBytes < 0) {
					// 对端链路关闭
					eventReactor.unRegisterEventInterestOps(socketChannel);
					socketChannel.close();
					brokenCallback.doChannelBroken();
					return;
				}

				totalReadBytes += readBytes;
				if (receivingPacketUnit.headBytBuffer.hasRemaining()) {
					// need wait remain head data;
					return;
				} else {
					// head read complete.
					receivingPacketUnit.headBytBuffer.flip();
					PacketHeader header = PacketHeader.readFromByteBuffer(receivingPacketUnit.headBytBuffer);
					receivingPacketUnit.bodyBytBuffer = ByteBuffer.allocate(header.bodyLength);
					receivingPacketUnit.packet = new Packet(header, receivingPacketUnit.bodyBytBuffer.array());

					// clean

				}
			} else {
				// body read
				int readBytes = socketChannel.read(receivingPacketUnit.bodyBytBuffer);
				if (readBytes < 0) {
					// 对端链路关闭
					eventReactor.unRegisterEventInterestOps(socketChannel);
					socketChannel.close();
					brokenCallback.doChannelBroken();
					return;
				}

				totalReadBytes += readBytes;

				if (receivingPacketUnit.bodyBytBuffer.hasRemaining()) {
					// need wait remain head data;
					return;
				} else {
					// body read complete.
					receivingPacketUnit.bodyBytBuffer.flip();
					lastReadPacketNo = receivingPacketUnit.packet.header.packetNo;
					logger.info(" receive a packet success , packetNo:" + receivingPacketUnit.packet.header.packetNo);

					totalReadPacket++;
					receivedCallback.handleReceivedPacket(receivingPacketUnit.packet);

					receivingPacketUnit.packet = null;
					receivingPacketUnit.headBytBuffer.clear();

				}
			}
		}
	}

}
