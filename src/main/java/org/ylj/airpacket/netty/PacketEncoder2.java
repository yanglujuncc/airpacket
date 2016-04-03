package org.ylj.airpacket.netty;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.log4j.Logger;
import org.ylj.airpacket.protocol.Packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class PacketEncoder2 extends MessageToMessageEncoder<Packet>{
	private static final Logger logger = Logger.getLogger(PacketEncoder2.class);
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg, List<Object> out) throws Exception {
		
		logger.info("encode packet:"+msg.header.packetNo);
		ByteBuffer bf=msg.toByteBuffer();
		ByteBuf bf2=ctx.alloc().buffer(bf.remaining());
		bf2.writeBytes(bf);
		out.add(bf2);
	}

}
