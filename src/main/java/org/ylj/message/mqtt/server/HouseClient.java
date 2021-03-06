package org.ylj.message.mqtt.server;


import org.apache.log4j.xml.DOMConfigurator;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.ylj.message.im.CoreJsonMsg;
import org.ylj.message.im.IMMsg;

import com.alibaba.fastjson.JSON;

public class HouseClient {
	
	  public static void main(String[] args) throws Exception {
		  
			DOMConfigurator.configure("conf/log4j.xml");

			
		  	MQTT mqtt = new MQTT();
			mqtt.setHost("localhost", 1883);
			// or 
			mqtt.setHost("tcp://localhost:1883");
			mqtt.setClientId("clientId_2");
			mqtt.setUserName("name_1");
			mqtt.setPassword("passwd_1");
			mqtt.setVersion("3.1.1");
			
			System.out.println(JSON.toJSONString(mqtt, true));
			
			System.out.println("try to connect server...");
			BlockingConnection connection = mqtt.blockingConnection();
	
			connection.connect();			
			System.out.println("connect server success.");
			
		
			String userId="ylj";
		
			Topic[] topics = { new Topic("clients/"+userId+"/down", QoS.AT_LEAST_ONCE) };
			byte[] qoses = connection.subscribe(topics);
			System.out.println("subscribe topic "+" success.");
			
			
			while (true) {
				
				System.out.println("waiting msg....");
				
				Message message = connection.receive();
			
				byte[] payload = message.getPayload();
				// process the message then:
			
				System.out.println("receive "+message.getTopic()+" "+new String(payload));
			
				message.ack();
			
				String coreJsonMsgJsonString=new String(payload,"utf-8");
				
				CoreJsonMsg coreJsonMsg=JSON.parseObject(coreJsonMsgJsonString, CoreJsonMsg.class);
				System.out.println("receive CoreJsonMsg, type:"+coreJsonMsg.msgType);
				
				if(coreJsonMsg.msgType==11){
					IMMsg imMsg=JSON.parseObject(coreJsonMsg.jsonContent, IMMsg.class);
					System.out.println(JSON.toJSONString(imMsg, true));
					
					IMMsg replyIMMsg=new IMMsg();
					replyIMMsg.from=imMsg.to;
					replyIMMsg.to=imMsg.from;
					replyIMMsg.content="reply of "+imMsg.content;
					
					CoreJsonMsg replyCoreJsonMsg=new CoreJsonMsg();
					replyCoreJsonMsg.msgType=11;
					replyCoreJsonMsg.jsonContent=JSON.toJSONString(replyIMMsg);
					
					String replyCoreJsonMsgJsonString=JSON.toJSONString(replyCoreJsonMsg);
					
					connection.publish("clients/"+replyIMMsg.to+"/up", replyCoreJsonMsgJsonString.getBytes(), QoS.AT_LEAST_ONCE, false);
					
					System.out.println("send reply success");
				}
				
			}
			//
			//connection.publish("foo", "Hello from client1".getBytes(), QoS.AT_LEAST_ONCE, false);
			//System.out.println("publish topic success .");
			/*
			
			Topic[] topics = {new Topic("foo", QoS.AT_LEAST_ONCE)};
			byte[] qoses = connection.subscribe(topics);
			Message message = connection.receive();
			System.out.println(message.getTopic());
			byte[] payload = message.getPayload();
			// process the message then:
			message.ack();
			*/
			//connection.disconnect();
	  }
	
}
