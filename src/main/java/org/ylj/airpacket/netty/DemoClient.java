package org.ylj.airpacket.netty;

import java.util.Date;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
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
	        ByteBuf m = (ByteBuf) msg; // (1)
	        try {
	            long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
	            System.out.println(new Date(currentTimeMillis));
	            ctx.close();
	        } finally {
	            m.release();
	        }
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        cause.printStackTrace();
	        ctx.close();
	    }
	}
	  public  static class TimeClientHandler2 extends ChannelInboundHandlerAdapter {
		    private ByteBuf buf;

		    @Override
		    public void handlerAdded(ChannelHandlerContext ctx) {
		        buf = ctx.alloc().buffer(4); // (1)
		    }

		    @Override
		    public void handlerRemoved(ChannelHandlerContext ctx) {
		        buf.release(); // (1)
		        buf = null;
		    }

		    @Override
		    public void channelRead(ChannelHandlerContext ctx, Object msg) {
		        ByteBuf m = (ByteBuf) msg;
		        buf.writeBytes(m); // (2)
		        m.release();

		        if (buf.readableBytes() >= 4) { // (3)
		            long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L;
		            System.out.println(new Date(currentTimeMillis));
		            ctx.close();
		        }
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
	                    ch.pipeline().addLast(new TimeClientHandler2());
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
