package org.ylj.message.mqtt;

import org.apache.log4j.Logger;

import io.moquette.spi.security.IAuthenticator;

public class CoreMsgMQTTAuthenticator implements IAuthenticator {
	private static final Logger logger = Logger.getLogger(CoreMsgMQTTAuthenticator.class);

	public CoreMsgMQTTAuthenticator(){
		logger.info("MyAuthenticator created");
	}
	@Override
	public boolean checkValid(String username, byte[] password) {
		logger.info("checkValid username:"+username+" password:"+new String(password));
		return true;
		//return false;
	}

}
