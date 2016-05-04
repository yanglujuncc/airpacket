package org.ylj.message.mqtt;

import org.apache.log4j.Logger;

import io.moquette.spi.security.IAuthorizator;


public class CoreMsgMQTTAuthorizator implements IAuthorizator{

	
	private static final Logger logger = Logger.getLogger(CoreMsgMQTTAuthorizator.class);

	public CoreMsgMQTTAuthorizator(){
		logger.info("MyAuthorizator created");
	}
	@Override
	public boolean canRead(String topic, String user, String client) {
		logger.info("canRead topic:"+topic+" user:"+user+" client:"+client);
		return true;
		
	}

	@Override
	public boolean canWrite(String topic, String user, String client) {
		logger.info("canWrite topic:"+topic+" user:"+user+" client:"+client);
		
		return true;
	}

}
