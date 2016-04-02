package ylj.demo.network.netty4;

import java.util.Date;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;


public class DemoClient {

	  public static class TimeClientHandler extends ChannelInboundHandlerAdapter {
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) {
	    	System.out.println("===> TimeClientHandler.channelRead() "+msg.getClass());
	    	
	    	DemoUnixTime m = (DemoUnixTime) msg;
	        System.out.println(m);
	      //  ctx.close();
	        ctx.fireChannelRead(msg);
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        cause.printStackTrace();
	        ctx.close();
	    }
	}

	  public static class TimeClientHandler2 extends ChannelInboundHandlerAdapter {
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) {
	    	System.out.println("===> TimeClientHandler2.channelRead() "+msg.getClass());
	    	
	    	DemoUnixTime m = (DemoUnixTime) msg;
	        System.out.println(m);
	        ctx.fireChannelRead(msg);
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        cause.printStackTrace();
	        ctx.close();
	    }
	}
	  public static void main(String[] args) throws Exception {
		   
		   String host="localhost";
		   int port=8080;
	        
	        EventLoopGroup workerGroup = new NioEventLoopGroup();

	        try {
	            Bootstrap b = new Bootstrap(); // (1)
	            b.group(workerGroup); // (2)
	            b.channel(NioSocketChannel.class); // (3)
	            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
	            b.handler(new ChannelInitializer<SocketChannel>() {
	                @Override
	                public void initChannel(SocketChannel ch) throws Exception {
	                //  
	                    ch.pipeline().addLast(new DemoUnixTimeDecoder());
	                    ch.pipeline().addLast(new TimeClientHandler2());
	                    ch.pipeline().addLast(new TimeClientHandler());
		                  
	                }
	            });

	            // Start the client.
	            ChannelFuture f = b.connect(host, port).sync(); // (5)

	            // Wait until the connection is closed.
	            f.channel().closeFuture().sync();
	        } finally {
	            workerGroup.shutdownGracefully();
	        }
	    }
}
