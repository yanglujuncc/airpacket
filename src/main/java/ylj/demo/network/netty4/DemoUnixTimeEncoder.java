package ylj.demo.network.netty4;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class DemoUnixTimeEncoder  extends MessageToByteEncoder<DemoUnixTime>{
	private static final Logger logger = Logger.getLogger(DemoUnixTimeDecoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, DemoUnixTime msg, ByteBuf out) throws Exception {
		logger.info("===> DemoUnixTimeDecoder.encode()");
		out.writeInt((int)msg.value());

		//ctx.pipeline().write(msg);
	}

}
