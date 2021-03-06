package ylj.demo.network.mqtt.moquette;


import org.apache.log4j.xml.DOMConfigurator;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import com.alibaba.fastjson.JSON;

public class DemoClient {
	
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
			connection.publish("clients/01/down", "Hello from client1".getBytes(), QoS.AT_LEAST_ONCE, false);
			connection.publish("foo", "Hello from client1".getBytes(), QoS.AT_LEAST_ONCE, false);
			System.out.println("publish topic success .");
			/*
			
			Topic[] topics = {new Topic("foo", QoS.AT_LEAST_ONCE)};
			byte[] qoses = connection.subscribe(topics);
			Message message = connection.receive();
			System.out.println(message.getTopic());
			byte[] payload = message.getPayload();
			// process the message then:
			message.ack();
			*/
			connection.disconnect();
	  }
	
}
