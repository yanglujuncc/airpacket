package org.ylj.airpacket.api.server;

import org.ylj.airpacket.api.AirMsg;

public interface ClientAirMsgReceiveHandler {
	public void doReceive(ClientContext context,  AirMsg msg);
}
