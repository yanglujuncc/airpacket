package org.ylj.airpacket.api.client;

import java.util.concurrent.Future;

import org.ylj.airpacket.api.AirMsg;

public interface AirMsgClient {
	
	public boolean linkTo(String host,int port,String ticket);
	public void setReceiveMsgHandler(AirMsgReceiveHandler receiveHandler);
	public Future<Void> sendMsg(AirMsg airMsg);
	public void sendMsgAsync(AirMsg airMsg,AirMsgSendCallbackHandler callbackHandler);


}
