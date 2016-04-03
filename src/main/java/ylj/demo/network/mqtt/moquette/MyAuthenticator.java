package ylj.demo.network.mqtt.moquette;

import org.apache.log4j.Logger;

import io.moquette.spi.security.IAuthenticator;

public class MyAuthenticator implements IAuthenticator {
	private static final Logger logger = Logger.getLogger(MyAuthenticator.class);

	public MyAuthenticator(){
		logger.info("MyAuthenticator created");
	}
	@Override
	public boolean checkValid(String username, byte[] password) {
		logger.info("checkValid username:"+username+" password:"+new String(password));
		return true;
		//return false;
	}

}
