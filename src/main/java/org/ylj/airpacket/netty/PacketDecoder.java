package org.ylj.airpacket.netty;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.log4j.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.ylj.airpacket.netty.PacketDecoder.PacketDecoderState;
import org.ylj.airpacket.protocol.Packet;
import org.ylj.airpacket.protocol.PacketHeader;

/**
 * every channel has its PacketDecoder
 * @author yanglujun
 *
 */
public class PacketDecoder  extends ReplayingDecoder<PacketDecoderState>{
	
	private static final Logger logger = Logger.getLogger(PacketDecoder.class);
	
	  enum PacketDecoderState {
	        READ_FIXED_HEADER,
	        READ_PAYLOAD
	    }
	
	ByteBuffer headBytBuffer;
	ByteBuffer bodyBytBuffer;
	PacketHeader header;
	
	PacketDecoder(){
		state(PacketDecoderState.READ_FIXED_HEADER);
		headBytBuffer=ByteBuffer.allocate(PacketHeader.ByteSize);
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		
		while(true){
		   switch (state()) {
		   		case READ_FIXED_HEADER:
		   			
		   			in.readBytes(headBytBuffer);
		   			checkpoint();
		   			
		   			if(headBytBuffer.hasRemaining()){		   				
		   				return ;
		   			}else{
		   				headBytBuffer.flip();
		   				header=PacketHeader.readFromByteBuffer(headBytBuffer);
		   				bodyBytBuffer=ByteBuffer.allocate(header.bodyLength);
		   				state(PacketDecoderState.READ_PAYLOAD);
		   			}
		   
		   		case READ_PAYLOAD:
		   			
		   			in.readBytes(bodyBytBuffer);
		   			checkpoint();
		   			
		   			if(bodyBytBuffer.hasRemaining()){		   				
		   				return ;
		   			}else{ 				
		   				bodyBytBuffer.flip();
		   				Packet packet =new Packet(header,bodyBytBuffer.array());
		   				out.add(packet);
		   				
		   				//begin to receive anther packet 
		   				headBytBuffer.clear();
		   				headBytBuffer=null;
		   				state(PacketDecoderState.READ_FIXED_HEADER);		   			
		   			}
			   
		   }
		}
		
	}
	

}
