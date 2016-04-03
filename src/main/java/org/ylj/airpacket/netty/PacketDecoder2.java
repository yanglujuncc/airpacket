package org.ylj.airpacket.netty;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.log4j.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import org.ylj.airpacket.netty.PacketDecoder2.PacketDecoderState;
import org.ylj.airpacket.protocol.Packet;
import org.ylj.airpacket.protocol.PacketHeader;

/**
 * every channel has its PacketDecoder
 * @author yanglujun
 *
 */
public class PacketDecoder2  extends ByteToMessageDecoder{
	
	private static final Logger logger = Logger.getLogger(PacketDecoder2.class);
	
	  enum PacketDecoderState {
	        READ_FIXED_HEADER,
	        READ_PAYLOAD
	    }
	 
	PacketDecoderState state;
	  
	ByteBuffer headBytBuffer;
	ByteBuffer bodyBytBuffer;
	PacketHeader header;
	
	PacketDecoder2(){
		state=PacketDecoderState.READ_FIXED_HEADER;
		headBytBuffer=ByteBuffer.allocate(PacketHeader.ByteSize);
		headBytBuffer.clear();
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		
		try{
		while(true){
		   switch (state) {
		   		case READ_FIXED_HEADER:
		   			logger.info("READ_FIXED_HEADER in.readableBytes():"+in.readableBytes());
		   			if(in.readableBytes()<=0){
		   				return ;
		   			}
		   			in.readBytes(headBytBuffer);
		   			
		   			if(headBytBuffer.hasRemaining()){		   				
		   				return ;
		   			}else{
		   				headBytBuffer.flip();
		   				header=PacketHeader.readFromByteBuffer(headBytBuffer);
		   				bodyBytBuffer=ByteBuffer.allocate(header.bodyLength);
		   				state=PacketDecoderState.READ_PAYLOAD;
		   			}
		   
		   		case READ_PAYLOAD:
		   			logger.info("READ_PAYLOAD in.readableBytes():"+in.readableBytes());
		   			if(in.readableBytes()<=0){
		   				return ;
		   			}
		   			in.readBytes(bodyBytBuffer);
		   			if(bodyBytBuffer.hasRemaining()){		   				
		   				return ;
		   			}else{ 				
		   				bodyBytBuffer.flip();
		   				Packet packet =new Packet(header,bodyBytBuffer.array());
		   				out.add(packet);
		   				
		   				//begin to receive anther packet 
		   				headBytBuffer.clear();
		   				bodyBytBuffer=null;
		   				state=PacketDecoderState.READ_FIXED_HEADER;		   			
		   			}
			   
		   }
		}
		}finally{
			//in.release();
		}
		
	}
	

}
