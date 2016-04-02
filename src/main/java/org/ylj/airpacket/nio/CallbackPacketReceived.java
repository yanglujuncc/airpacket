package org.ylj.airpacket.nio;

import org.ylj.airpacket.protocol.Packet;

public interface CallbackPacketReceived {
	public void handleReceivedPacket(Packet packet);
}
