package org.ylj.airpacket.protocol.server.nio;


public interface CallbackAirMsgServerPacketSend {
	public void doSendSuccess();
	public void doSendFailure();
}
