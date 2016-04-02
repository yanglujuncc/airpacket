/**
 *  @author hzyanglujun
 *  @version  创建时间:2016年3月18日 下午1:08:19
 */
package ylj.demo.network.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

/**
 * @author hzyanglujun
 *
 */
public class TimeClientHandle implements Runnable {
	 private static final Logger logger = Logger.getLogger(TimeClientHandle.class);

	private String host;
	private int port;
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop;

//	private ConcurrentLinkedQueue<ByteBuffer> toWriteQueue=new ConcurrentLinkedQueue<ByteBuffer>();
	
	//public static class ToWrite{
		
	//}
	public TimeClientHandle(String host, int port) {
		this.host = host == null ? "127.0.0.1" : host;
		this.port = port;
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			doConnect();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		while (!stop) {
			try {
				selector.select(1000);
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();
				SelectionKey key = null;
				while (it.hasNext()) {
					key = it.next();
					it.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						if (key != null) {
							key.cancel();
							if (key.channel() != null)
								key.channel().close();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		// 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
		if (selector != null)
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	static int sendReqCounter=0;
	private void handleInput(SelectionKey key) throws IOException {

		if (key.isValid()) {
			// 判断是否连接成功
			SocketChannel sc = (SocketChannel) key.channel();
			sc.isBlocking();
			
			System.out.println("sc.isBlocking():"+sc.isBlocking());
			
			if (key.isConnectable()) {
				System.out.println(" key.isConnectable() ");
				
				if (sc.finishConnect()) {
					sc.register(selector, SelectionKey.OP_READ);
					doWrite(sc);
				} else
					System.exit(1);// 连接失败，进程退出
			}
			if (key.isReadable()) {
				System.out.println(" key.isReadable() ");
				
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);
				if (readBytes > 0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, "UTF-8");
					System.out.println("Now is : " + body);
					
					if(reqCounter==2){
						this.stop = true;
					}else{
						try {
							Thread.sleep(2000);
							doWrite(sc);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				} else if (readBytes < 0) {
					// 对端链路关闭
					key.cancel();
					sc.close();
				} else
					; // 读到0字节，忽略
			}

			if (key.isWritable()) {
				System.out.println(" key.isWritable() ");	
				doWrite( sc);
					
			}
		}

	}

	private void doConnect() throws IOException {
		// 如果直接连接成功，则注册到多路复用器上，发送请求消息，读应答
		if (socketChannel.connect(new InetSocketAddress(host, port))) {
			socketChannel.register(selector, SelectionKey.OP_READ);
			doWrite(socketChannel);
		} else
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
	}

	static int reqCounter=0;
	private void doWrite(SocketChannel sc) throws IOException {
		reqCounter++;
		byte[] req = ("QUERY TIME ORDER req："+reqCounter).getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		int writen=sc.write(writeBuffer);  //just write to the socket buffer,
		System.out.println("writen:"+writen);
		if (!writeBuffer.hasRemaining()){
			System.out.println("try Send req to server succeed.");
		}else{
			//System.out.println("try Send req to server failed. add to write queue");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}
		new Thread(new TimeClientHandle("127.0.0.1", port), "TimeClient-001").start();
	}
}
