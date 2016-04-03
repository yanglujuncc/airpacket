package org.ylj.airpacket.netty;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.ylj.airpacket.nio.example.SendPacketExample;
import org.ylj.airpacket.protocol.Packet;

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

import io.netty.channel.ChannelFutureListener;

public class DemoClient {

	private static final Logger logger = Logger.getLogger(DemoClient.class);
		public static class SendPacketListener implements ChannelFutureListener{
			Packet packet;
			public SendPacketListener(	Packet packet){
				this.packet=packet;
			}
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				logger.info(" write packet:"+packet.header.packetNo+" succeess");
			}
		}
	   public static void main(String[] args) throws Exception {
			DOMConfigurator.configure("conf/log4j.xml");

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
	                    ch.pipeline().addLast(new PacketDecoder2());
	                    ch.pipeline().addLast(new PacketEncoder2());
	                    ch.pipeline().addLast(new PacketReaderHandler());
	                }
	            });

	            // Start the client.
	            ChannelFuture f = b.connect(host, port).sync(); // (5)

	            if(f.isDone()){
	            	for(int i=0;i<100;i++){
	            		Thread.sleep(3000);
	            		Packet packet=SendPacketExample.getNextPacket();
	            		logger.info("try to write a packet:"+packet.header.toString());
	            		ChannelFuture cf=f.channel().writeAndFlush(packet);
	            		cf.addListener(new SendPacketListener(packet));	            		
	            		//cf.await();
	            		//System.out.println("send a packet success:"+packet.header.toString());
	            	}
	            }
	      //      b.
	            
	            // Wait until the connection is closed.
	            f.channel().closeFuture().sync();
	        } finally {
	            workerGroup.shutdownGracefully();
	        }
	    }
}
