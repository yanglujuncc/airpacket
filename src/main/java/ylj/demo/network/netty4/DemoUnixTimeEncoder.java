package ylj.demo.network.netty4;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class DemoUnixTimeEncoder  extends MessageToByteEncoder<DemoUnixTime>{

	@Override
	protected void encode(ChannelHandlerContext ctx, DemoUnixTime msg, ByteBuf out) throws Exception {
		System.out.println("===> DemoUnixTimeDecoder.encode()");
		out.writeInt((int)msg.value());

		//ctx.pipeline().write(msg);
	}

}
