package org.ylj.airpacket.protocol.server.nio;

import org.ylj.airpacket.protocol.Packet;

public interface CallbackAirMsgServerPacketReceived {
	public void handleReceivedPacket(Packet packet);
}
