package ylj.demo.network.netty4;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;


public class DemoUnixTimeDecoder  extends ByteToMessageDecoder{

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		 
		System.out.println("===> DemoUnixTimeDecoder.decode() readableBytes:"+in.readableBytes());
		if (in.readableBytes() < 4) {
	            return; // (3)
	     }
		 
		 out.add(new DemoUnixTime(in.readUnsignedInt()));
		
	}

	

}
