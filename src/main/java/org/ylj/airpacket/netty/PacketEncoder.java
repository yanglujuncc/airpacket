package org.ylj.airpacket.netty;

import org.apache.log4j.Logger;
import org.ylj.airpacket.protocol.Packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder  extends MessageToByteEncoder<Packet>{
	private static final Logger logger = Logger.getLogger(PacketDecoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
		logger.info("===> do encode()");
	//	if(out.writableBytes())
		out.writeBytes(msg.toByteBuffer().array());
	
	}

}
