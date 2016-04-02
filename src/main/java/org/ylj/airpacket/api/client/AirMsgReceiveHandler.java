package org.ylj.airpacket.api.client;

import org.ylj.airpacket.api.AirMsg;

public interface AirMsgReceiveHandler {
	public void doReceive(AirMsg msg);
}
