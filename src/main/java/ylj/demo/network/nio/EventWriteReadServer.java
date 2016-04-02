/**
 *  @author hzyanglujun
 *  @version  创建时间:2016年3月18日 上午11:00:07
 */
package ylj.demo.network.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;



/**
 * @author hzyanglujun
 *
 */
public class EventWriteReadServer implements Runnable {
	
	 private static final Logger logger = Logger.getLogger(EventWriteReadServer.class);

	private Selector selector;

	private ServerSocketChannel servChannel;

	private volatile boolean stop;

	/**
	 * 初始化多路复用器、绑定监听端口
	 * 
	 * @param port
	 */
	public EventWriteReadServer(int port) {
		try {
			selector = Selector.open();
			servChannel = ServerSocketChannel.open();
			servChannel.configureBlocking(false);
			servChannel.socket().bind(new InetSocketAddress(port), 1024);
			servChannel.register(selector, SelectionKey.OP_ACCEPT);
			logger.info("The time server is start in port :" + port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void stop() {
		this.stop = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		int loop=0;
		
		long lastTotalRead=totalRead;
		long lastTime=System.currentTimeMillis();
		while (!stop) {
			try {
				loop++;
				
				selector.select(5000);
			
			
					
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				long thisRead=totalRead;
				long thisTime=System.currentTimeMillis();
				long readAdd=thisRead-lastTotalRead;
				long timeAdd=thisTime-lastTime;
				long speedQPS=0;
				if(readAdd>0){
					if(timeAdd!=0)
						speedQPS=readAdd/timeAdd;
					else
						speedQPS=9999999999999L;
				}
				lastTotalRead=thisRead;
				lastTime=thisTime;
				logger.info("select loop("+loop+") , selectedKeys:"+selectedKeys.size()+" readAdd:"+readAdd/1000+"KB totalRead:"+totalRead/1000+"KB speedQPS:"+speedQPS+"KB/s");
				
				Iterator<SelectionKey> it = selectedKeys.iterator();
				SelectionKey key = null;
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					try {
						handleEvent(key);
					} catch (Exception e) {
						if (key != null) {
							key.cancel();
							if (key.channel() != null)
								key.channel().close();
						}
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		// 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static long totalRead=0;
	private void handleEvent(SelectionKey key) throws IOException {

		if (key.isValid()) {
			// 处理新接入的请求消息
			
		
			if (key.isAcceptable()) {
				logger.info(" key.isAcceptable() ");
				// Accept the new connection
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				// Add the new connection to the selector
				sc.register(selector, SelectionKey.OP_READ);
			}
			if (key.isReadable()) {
			//	logger.info(" key.isReadable() ");
				// Read the data
				SocketChannel sc = (SocketChannel) key.channel();
				int thisLoopRead=0;
				while(true){
					ByteBuffer readBuffer = ByteBuffer.allocate(1024);
					int readBytes = sc.read(readBuffer);
					thisLoopRead+=readBytes;
					if (readBytes == 1024) {
						readBuffer.flip();
						//do something...
						continue;
					}
					
					if (readBytes > 0) {
						readBuffer.flip();
					
					} else if (readBytes < 0) {
						// 对端链路关闭
						key.cancel();
						sc.close();
					} else {
						; // 读到0字节，忽略
					}
					
					break;
				}
				
				totalRead+=thisLoopRead;

			}
			if (key.isWritable()) {
				logger.info(" key.isWritable() ");
			}
			if (key.isConnectable()) {
				logger.info(" key.isConnectable() ");
			}
		}
	}

	private void doWrite(SocketChannel channel, String response) throws IOException {

		if (response != null && response.trim().length() > 0) {
			byte[] bytes = response.getBytes();
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			channel.write(writeBuffer);
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		

		DOMConfigurator.configure("conf/log4j.xml");

		int port = 8080;

		EventWriteReadServer timeServer = new EventWriteReadServer(port);
		new Thread(timeServer, "MultiplexerTimeServer").start();
	}
}
