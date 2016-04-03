package ylj.demo.network.mqtt.moquette;

import org.apache.log4j.Logger;

import io.moquette.spi.security.IAuthorizator;


public class MyAuthorizator implements IAuthorizator{

	
	private static final Logger logger = Logger.getLogger(MyAuthorizator.class);

	public MyAuthorizator(){
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
