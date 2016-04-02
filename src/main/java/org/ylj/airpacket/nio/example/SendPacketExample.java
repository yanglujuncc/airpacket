package org.ylj.airpacket.nio.example;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.xml.DOMConfigurator;
import org.ylj.airpacket.nio.AirPacketConnection;
import org.ylj.airpacket.nio.AirPacketConnectionCreator;
import org.ylj.airpacket.nio.CallbackConnectionBroken;
import org.ylj.airpacket.nio.CallbackPacketSend;
import org.ylj.airpacket.nio.EventReactor;
import org.ylj.airpacket.protocol.Packet;
import org.ylj.airpacket.protocol.PacketHeader;
import org.ylj.airpacket.protocol.PacketType;

public class SendPacketExample {
	
	public static int nextPacketNo=1;
	
	public static Packet getNextPacket(){
		
		PacketHeader header=new PacketHeader();
		
		header.version=1;
		header.type=PacketType.DATA;
		
		header.packetNo=nextPacketNo++;
		header.rePacketNo=0;
		
		header.vChanel=2;
		
		
		byte[] body="hello".getBytes();
		header.bodyLength=body.length;
		
		Packet packet=new Packet(header,body);
		
		return packet;
	}
	public static class SimpleCallbackConnectionBroken implements CallbackConnectionBroken{

		@Override
		public void doChannelBroken() {
			// TODO Auto-generated method stub
			
		}
		
	}
	public static class SimpleCallbackAirMsgClientPacketSend implements CallbackPacketSend{

		Packet packet;
		
		public SimpleCallbackAirMsgClientPacketSend(Packet packet){
			this.packet=packet;
		}
		@Override
		public void doSendSuccess() {
			System.out.println("success, send packet  , packet:"+packet.header);
			
		}

		@Override
		public void doSendFailure() {
			System.out.println("failed, send packet  , packet:"+packet.header);
			
		}
		
	}
	public static void main(String[] args) throws ClosedChannelException, IOException, InterruptedException, ExecutionException {

		DOMConfigurator.configure("conf/log4j.xml");

		EventReactor eventReactor = new EventReactor();
		eventReactor.start();
		
		String host="localhost";
		int port=8080;
		
		Future<AirPacketConnection> airMsgClientFuture =AirPacketConnectionCreator.createClient(host, port, eventReactor);
		AirPacketConnection client=airMsgClientFuture.get();
		if(client==null){
			System.out.println("get client failed ,"+client);
			System.exit(1);
		}
		System.out.println("get client success ,"+client);
		client.setEventReactor(eventReactor);
		
		for(int i=0;i<10000;i++){
			Packet packet= getNextPacket();
			client.sendPacket(packet, new SimpleCallbackAirMsgClientPacketSend(packet));
			System.out.println("to send packet  , packet-id:"+packet.header.packetNo);
		}
		
	}
}
