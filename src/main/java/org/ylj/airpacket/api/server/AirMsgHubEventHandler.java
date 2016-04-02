package org.ylj.airpacket.api.server;

public interface AirMsgHubEventHandler {
	
	
	public void newConnectClient(ClientContext client);
	public void disconnectClient(ClientContext client);

}
