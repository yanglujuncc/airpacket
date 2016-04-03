package ylj.demo.network.mqtt.moquette;


import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

public class DemoClient2 {
	
	  public static void main(String[] args) throws Exception {
		  
		   final String Topic="foo";
		  
		  	MQTT mqtt = new MQTT();
			mqtt.setHost("localhost", 1883);
			// or 
			mqtt.setHost("tcp://localhost:1883");
			mqtt.setClientId("clientId_3");
			mqtt.setUserName("name_3");
			mqtt.setPassword("passwd_3");
			mqtt.setVersion("3.1.1");
			
			final CallbackConnection connection = mqtt.callbackConnection();
			
			connection.listener(new Listener() {

			    public void onDisconnected() {
			    }
			    public void onConnected() {
			    }

			    public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
			        // You can now process a received message from a topic.
			        // Once process execute the ack runnable.
			    	  System.out.println(" do onPublish");

					    
			        System.out.println(" on  topic:" + topic.toString()+ " content: " + new String(payload.data));

			    
					// process the message then:
					
			        ack.run();
			    }
			    public void onFailure(Throwable value) {
			    	value.printStackTrace();
			    }
			});
			connection.connect(new Callback<Void>() {
			    public void onFailure(Throwable value) {
			      value.printStackTrace();
			    }

			    // Once we connect..
			    public void onSuccess(Void v) {
			    	System.out.println("connect  success.");
			        // Subscribe to a topic
			        Topic[] topics = {new Topic(Topic, QoS.AT_LEAST_ONCE)};
			        connection.subscribe(topics, new Callback<byte[]>() {
			            public void onSuccess(byte[] qoses) {
			                // The result of the subcribe request.
			            	System.out.println("subscribe topic "+Topic+" success.");
			            }
			            public void onFailure(Throwable value) {
			            	value.printStackTrace();
			            //	connection.
			               // connection.close(null); // subscribe failed.
			            }
			        });
			        /*
			        // Send a message to a topic
			        connection.publish("foo", "Hello".getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
			            public void onSuccess(Void v) {
			              // the pubish operation completed successfully.
			            }
			            public void onFailure(Throwable value) {
			            	value.printStackTrace();
			               // connection.close(null); // publish failed.
			            }
			        });

			        // To disconnect..
			        connection.disconnect(new Callback<Void>() {
			            public void onSuccess(Void v) {
			              // called once the connection is disconnected.
			            }
			            public void onFailure(Throwable value) {
			              // Disconnects never fail.
			            }
			        });
			        
			        */
			    }
			});
	 
			Thread.sleep(10000000);
			System.out.println("out");
			
			//connection.
	  }
	
}
