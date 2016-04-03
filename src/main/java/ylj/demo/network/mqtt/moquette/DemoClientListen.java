package ylj.demo.network.mqtt.moquette;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

public class DemoClientListen {

	public static void main(String[] args) throws Exception {
		
		MQTT mqtt = new MQTT();
		mqtt.setHost("localhost", 1883);
		// or
		mqtt.setHost("tcp://localhost:1883");
		mqtt.setClientId("clientId_2");
		mqtt.setUserName("name_2");
		mqtt.setPassword("passwd_2");
		mqtt.setVersion("3.1.1");

		System.out.println("try to connect server...");
		BlockingConnection connection = mqtt.blockingConnection();

		connection.connect();
		System.out.println("connect server success.");

		// connection.publish("foo", "Hello from client1".getBytes(),
		// QoS.AT_LEAST_ONCE, false);
		// System.out.println("publish topic success .");
		Topic[] topics = { new Topic("foo", QoS.AT_LEAST_ONCE) };
		byte[] qoses = connection.subscribe(topics);
		System.out.println("subscribe topic "+"foo"+" success.");
		
		
		while (true) {
				Message message = connection.receive();
			
			byte[] payload = message.getPayload();
			// process the message then:
			
			System.out.println("receive "+message.getTopic()+" "+new String(payload));
			
			message.ack();
		}
	//	connection.disconnect();
	}

}
