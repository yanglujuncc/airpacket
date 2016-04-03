package ylj.demo.network.netty4;

import java.util.List;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;


public class DemoUnixTimeDecoder  extends ByteToMessageDecoder{
	private static final Logger logger = Logger.getLogger(DemoUnixTimeDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		 
		logger.info("===> DemoUnixTimeDecoder.decode() readableBytes:"+in.readableBytes());
		if (in.readableBytes() < 4) {
	            return; // (3)
	     }
		 
		 out.add(new DemoUnixTime(in.readUnsignedInt()));
		
	}

	

}
