package org.ylj.airpacket.protocol;

import java.nio.ByteBuffer;

public class PacketHeader {

	public byte version;
	public byte type;
	
	public int packetNo;
	public int rePacketNo;
	public int vChanel;
	public int bodyLength;

	public static final int ByteSize = 18;

	public int getByteSize() {
		return ByteSize;
	}
	public String toString(){
		return "version:"+version+" type:"+type+" packetNo:"+packetNo+" rePacketNo:"+rePacketNo+" vChanel:"+vChanel+" bodyLength:"+bodyLength;
	}
	public ByteBuffer writeToByteBuffer(ByteBuffer byteBuffer) {

	
		byteBuffer.put(version);
		byteBuffer.put(type);
		byteBuffer.putInt(packetNo);
		byteBuffer.putInt(rePacketNo);
		byteBuffer.putInt(vChanel);
		byteBuffer.putInt(bodyLength);
	
		return byteBuffer;

	}

	public  static PacketHeader readFromByteBuffer(ByteBuffer byteBuffer) {

		PacketHeader packetHeader = new PacketHeader();
		packetHeader.version = byteBuffer.get();
		packetHeader.type = byteBuffer.get();
		packetHeader.packetNo = byteBuffer.getInt();
		packetHeader.rePacketNo = byteBuffer.getInt();
		packetHeader.vChanel = byteBuffer.getInt();
		packetHeader.bodyLength = byteBuffer.getInt();

		return packetHeader;

	}

	public byte[] toBytes() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(ByteSize);
		writeToByteBuffer( byteBuffer);
		 byteBuffer.flip();
		return byteBuffer.array();

	}
}
