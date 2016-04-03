package ylj.demo.network.mqtt.moquette;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;

import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;

public class MyInterceptHandler implements InterceptHandler{
	private static final Logger logger = Logger.getLogger(MyInterceptHandler.class);

	@Override
	public void onConnect(InterceptConnectMessage msg) {
		
		logger.info("onConnect "+JSON.toJSONString(msg));		  
	}

	@Override
	public void onDisconnect(InterceptDisconnectMessage msg) {
		logger.info("onDisconnect "+JSON.toJSONString(msg));	
		
	}

	@Override
	public void onPublish(InterceptPublishMessage msg) {
		logger.info("onPublish client:"+msg.getClientID()+" topic:"+msg.getTopicName()+"body:"+new String(msg.getPayload().array()));	
		
		//logger.info("onPublish "+JSON.toJSONString(msg));	
		
	}

	@Override
	public void onSubscribe(InterceptSubscribeMessage msg) {
		logger.info("onSubscribe "+JSON.toJSONString(msg));	
			
	}

	@Override
	public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
		logger.info("onUnsubscribe "+JSON.toJSONString(msg));	
		
	}

}
