package org.ylj.message.mqtt;

import org.apache.log4j.Logger;
import org.ylj.message.im.CoreJsonMsg;
import org.ylj.message.im.CoreMsgTypes;
import org.ylj.message.im.IMMsg;

import com.alibaba.fastjson.JSON;

import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;

public class CoreMsgMQTTInterceptHandler implements InterceptHandler{
	
	
	private static final Logger logger = Logger.getLogger(CoreMsgMQTTInterceptHandler.class);
	 
    //上行upTopic
  //  let UpTopicName = "clients/01/up"
    //下行 downTopic
   // let DownTopicName = "clients/01/down"
    
	MQTTIMMsgRouter imMsgRouter;
	
	
	
	@Override
	public void onConnect(InterceptConnectMessage msg) {
		msg.getUsername();
		
		logger.info("onConnect "+JSON.toJSONString(msg));		  
	}

	@Override
	public void onDisconnect(InterceptDisconnectMessage msg) {
		logger.info("onDisconnect "+JSON.toJSONString(msg));	
		
	}

	@Override
	public void onPublish(InterceptPublishMessage msg) {
		logger.info("onPublish client:"+msg.getClientID()+" topic:"+msg.getTopicName()+",body:"+new String(msg.getPayload().array()));	
		
		CoreMsgMQTTTopicName coreMsgTopicName=CoreMsgMQTTTopicName.parse(msg.getTopicName());
		logger.info(JSON.toJSONString(coreMsgTopicName));
		
		if(coreMsgTopicName==null){
			logger.warn("not a coreMsgTopicName:"+msg.getTopicName());
			return ;
		}
		if(coreMsgTopicName.type==CoreMsgMQTTTopicName.Down){
			logger.info("jump down...");
			return ;
		}
		CoreJsonMsg coreJsonMsg=JSON.parseObject(new String(msg.getPayload().array()), CoreJsonMsg.class) ;
		if(coreJsonMsg.msgType==CoreMsgTypes.Type_IMMsg){
			
			//coreJsonMsg.jsonContent
			IMMsg imMsg=JSON.parseObject(coreJsonMsg.jsonContent,IMMsg.class);
			try {
				imMsgRouter.route(imMsg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
