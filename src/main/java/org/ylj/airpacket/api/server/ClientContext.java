package org.ylj.airpacket.api.server;

public class ClientContext{
	
	String remoteIP;
	int remotePort;
	String ticket;
	
	long lastSendTime;
	long lastReceiveTime;
	
	long receiveMsgCounter;
	long sendMsgCounter;
}
