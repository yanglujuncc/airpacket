package ylj.demo.network.mqtt.moquette;

import io.moquette.interception.InterceptHandler;
import io.moquette.server.Server;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.ClasspathConfig;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;

public class EmbeddedLauncher {
	

	    public static void main(String[] args) throws InterruptedException, IOException {
	    	
	    	DOMConfigurator.configure("conf/log4j.xml");

	        final IConfig classPathConfig = new ClasspathConfig();
	        classPathConfig.setProperty("authenticator_class", "ylj.demo.network.mqtt.moquette.MyAuthenticator");
	        classPathConfig.setProperty("authorizator_class", "ylj.demo.network.mqtt.moquette.MyAuthorizator");
		       
	        final Server mqttBroker = new Server();
	        List<? extends InterceptHandler> userHandlers =Arrays.asList(new MyInterceptHandler());
	        mqttBroker.startServer(classPathConfig, userHandlers);
	        
	        
	       // mqttBroker.
	        System.out.println("Broker started press [CTRL+C] to stop");
	        //Bind  a shutdown hook
	        Runtime.getRuntime().addShutdownHook(new Thread() {
	            @Override
	            public void run() {
	                System.out.println("Stopping broker");
	                mqttBroker.stopServer();
	                System.out.println("Broker stopped");
	            }
	        });

	        Thread.sleep(5000);
	        
	        /*
	        System.out.println("Before self publish");
	        PublishMessage message = new PublishMessage();
	        message.setTopicName("/exit");
	        message.setRetainFlag(true);
//	        message.setQos(AbstractMessage.QOSType.MOST_ONE);
//	        message.setQos(AbstractMessage.QOSType.LEAST_ONE);
	        message.setQos(AbstractMessage.QOSType.EXACTLY_ONCE);
	        message.setPayload(ByteBuffer.wrap("Hello World!!".getBytes()));
	        mqttBroker.internalPublish(message);
	        System.out.println("After self publish");
	        */
	        
	        
	    }
}
