package org.ylj.airpacket.protocol;

import java.nio.ByteBuffer;

public class Packet {
	
	public PacketHeader header;
	public byte[] body;
	
	public Packet(PacketHeader header,byte[] body){
		this.header=header;
		this.body=body;
	}
	public ByteBuffer toByteBuffer(){
		int size=header.getByteSize()+body.length;
		
		ByteBuffer byteBuffer=ByteBuffer.allocate(size);		
		header.writeToByteBuffer(byteBuffer);
		byteBuffer.put(body);
		byteBuffer.flip();
		return byteBuffer;
	}
}
