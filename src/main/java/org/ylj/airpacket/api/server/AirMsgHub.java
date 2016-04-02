package org.ylj.airpacket.api.server;

import java.util.List;
import java.util.concurrent.Future;

import org.ylj.airpacket.api.AirMsg;

public interface AirMsgHub {
	
	
	/***************   ******************/
	
	public void setEventHandler (AirMsgHubEventHandler eventHandler);
	public void setTicketChecker(AirMsgHubTicketChecker ticketChecker);
	
	/**************** control **************/
	
	public List<ClientContext>  getAllLinked();
	public void disconnect(String remoteHost,int remotePort);
	public void disconnect(String ticket);
	
	
	/**************** msg *****************/
	 
	public void setReceiveClientMsgHandler(ClientAirMsgReceiveHandler receiveHandler);
	public Future<Void> sendClientMsg(String host,int port,AirMsg airMsg);
	public Future<Void> sendClientMsg(String ticket,AirMsg airMsg);
	public void sendClientMsgAsync(String host,int port,AirMsg airMsg,ClientAirMsgSendCallbackHandler callbackHandler);
	public void sendClientMsgAsync(String ticket,AirMsg airMsg,ClientAirMsgSendCallbackHandler callbackHandler);

}
