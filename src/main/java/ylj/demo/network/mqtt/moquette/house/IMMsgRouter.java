package ylj.demo.network.mqtt.moquette.house;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import com.alibaba.fastjson.JSON;

public class IMMsgRouter {
	

	private static final Logger logger = Logger.getLogger(IMMsgRouter.class);


  	MQTT mqtt ;
  	BlockingConnection connection;
  	
  	public void init(String host,int port,String clientId,String userName,String passwd) throws Exception{
  		
	  	mqtt = new MQTT();
		mqtt.setHost(host, port);
		// or 	
		mqtt.setClientId(clientId);
		mqtt.setUserName(userName);
		mqtt.setPassword(passwd);
		mqtt.setVersion("3.1.1");
		
		//System.out.println(JSON.toJSONString(mqtt, true));
		
		//
		connection = mqtt.blockingConnection();
		logger.info("try to connect server...");
		connection.connect();			
		logger.info("connect server success.");
  	}
	public void route(IMMsg imMsg) throws Exception{
		
		CoreJsonMsg replyCoreJsonMsg=new CoreJsonMsg();
		replyCoreJsonMsg.msgType=11;
		replyCoreJsonMsg.jsonContent=JSON.toJSONString(imMsg);
		
		String replyCoreJsonMsgJsonString=JSON.toJSONString(replyCoreJsonMsg);
		
		CoreMsgTopicName aCoreMsgTopicName=new CoreMsgTopicName(CoreMsgTopicName.Down,imMsg.to);
		
		logger.info("replyCoreJsonMsgJsonString:"+replyCoreJsonMsgJsonString);
		logger.info("          coreMsgTopicName:"+aCoreMsgTopicName.toTopicNameStr());
		
		connection.publish(aCoreMsgTopicName.toTopicNameStr(), replyCoreJsonMsgJsonString.getBytes(), QoS.AT_LEAST_ONCE, false);
		
	}
	
	  public static void main(String[] args) throws Exception {
		  
			DOMConfigurator.configure("conf/log4j.xml");

		
		
	  }
}
