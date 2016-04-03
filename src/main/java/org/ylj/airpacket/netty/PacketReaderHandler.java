package org.ylj.airpacket.netty;

import org.ylj.airpacket.protocol.Packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PacketReaderHandler  extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
    	
      	Packet packet = (Packet) msg; // (1)
    	System.out.println("receive a packet:"+packet.header.toString());
    	  
     
        //ctx.close();
        
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
