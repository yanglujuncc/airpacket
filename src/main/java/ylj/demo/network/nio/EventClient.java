/**
 *  @author hzyanglujun
 *  @version  创建时间:2016年3月18日 下午1:08:19
 */
package ylj.demo.network.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * @author hzyanglujun
 *
 */
public class EventClient implements Runnable {

	private static final Logger logger = Logger.getLogger(EventClient.class);

	private String host;
	private int port;
	private Selector selector;
	private SocketChannel socketChannel;
	private SelectionKey selectionKey;
	private volatile boolean stop;
	static long totalWriten = 0;
	
	private ConcurrentLinkedQueue<ByteBuffer> readedQueue = new ConcurrentLinkedQueue<ByteBuffer>();

	Lock writeLock = new ReentrantLock();
	private ConcurrentLinkedQueue<ByteBuffer> toWriteQueue = new ConcurrentLinkedQueue<ByteBuffer>();

	public EventClient() {

	}

	public void connect(String host, int port) {
		this.host = host == null ? "127.0.0.1" : host;
		this.port = port;
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);

			// 如果直接连接成功，则注册到多路复用器上，发送请求消息，读应答
			if (socketChannel.connect(new InetSocketAddress(host, port))) {
				selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
				// doWrite(socketChannel);
			} else {
				selectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT);
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void evenSelectLoop() throws IOException {
		int loop = 0;
		while (!stop) {

			loop++;

			selector.select(1000);
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			logger.info("client select loop(" + loop + ")  selectedKeys:" + selectedKeys.size() + ", selectionKey:" + selectionKey + " interestOps:" + selectionKey.interestOps());

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

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			evenSelectLoop();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	private void handleEvent(SelectionKey key) throws IOException {

		if (key.isValid()) {
			// 判断是否连接成功
			SocketChannel sc = (SocketChannel) key.channel();
			sc.isBlocking();

			// logger.info("sc.isBlocking():" + sc.isBlocking());

			if (key.isConnectable()) {
				logger.info(" key.isConnectable() ");

				if (sc.finishConnect()) {
					selectionKey = sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					// doWrite(sc);
				} else
					System.exit(1);// 连接失败，进程退出
			}
			if (key.isReadable()) {

				// SelectionKey.OP_READ is always regist

				logger.info(" key.isReadable() ");
				doReadAll();

			}

			if (key.isWritable()) {
				logger.info(" key.isWritable() ");
				doWriteAll();

			}
		}

	}

	public void addWriteTask(ByteBuffer toWriteByteBuffer) throws ClosedChannelException {
		writeLock.lock();
		try {
			toWriteQueue.add(toWriteByteBuffer);
			selectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
			// socketChannel.r
		} finally {
			writeLock.unlock();
		}

	}

	

	private void doWriteAll() throws IOException {

		while (toWriteQueue.size() > 0) {
			ByteBuffer toWriteBuffer = toWriteQueue.peek();

			int writen = socketChannel.write(toWriteBuffer); // just write to
																// the socket
																// buffer,
			totalWriten += writen;
			logger.info("writen:" + writen + " totalWriten:" + totalWriten);
			if (!toWriteBuffer.hasRemaining()) {
				toWriteQueue.poll();
				logger.info(" write toWriteBuffer success , toWriteQueue:" + toWriteQueue.size());
				// remove head
			} else {
				logger.info("not send all data,stall need write , remaining:" + toWriteBuffer.remaining() + ", toWriteQueue:" + toWriteQueue.size());
				break;
			}

		}

		writeLock.lock();
		try {
			if (toWriteQueue.size() == 0) {
				// no write task any more;
				selectionKey.interestOps(SelectionKey.OP_READ);
			}

		} finally {
			writeLock.unlock();

		}

		logger.info(" key.isWritable() ");
		// doWrite(sc);
	}

	private void doReadAll() throws IOException {

		while (true) {

			int BufferSize = 1024;
			ByteBuffer readBuffer = ByteBuffer.allocate(BufferSize);
			int readBytes = socketChannel.read(readBuffer);

			if (readBytes == BufferSize) {
				readBuffer.flip();
				readedQueue.add(readBuffer);
				// may have more data
				continue;
			}

			// no more data

			if (readBytes > 0) {
				readBuffer.flip();
				readedQueue.add(readBuffer);
			} else if (readBytes < 0) {
				// 对端链路关闭
				selectionKey.cancel();
				socketChannel.close();
			} else
				; // 读到0字节，忽略

			break;
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws ClosedChannelException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws ClosedChannelException, IOException, InterruptedException {

		DOMConfigurator.configure("conf/log4j.xml");

		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				// 采用默认值
			}
		}
		EventClient client = new EventClient();
		for (int i = 0; i < 10; i++) {
			client.addWriteTask(nextWriteBuffer2(1024 * 1024));
		}

		client.connect("127.0.0.1", port);
		new Thread(client, "TimeClient-001").start();

	}

	static int reqCounter = 0;

	private static ByteBuffer nextWriteBuffer() throws IOException {
		reqCounter++;

		byte[] req = ("QUERY TIME ORDER req：" + reqCounter).getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		return writeBuffer;
	}

	private static ByteBuffer nextWriteBuffer2(int BufferSize) throws IOException {
		reqCounter++;

		ByteBuffer toWriteBuffer = ByteBuffer.allocate(BufferSize);
		for (int i = 0; i < BufferSize; i++) {
			byte b = 3;
			toWriteBuffer.put(b);
		}

		toWriteBuffer.flip();
		return toWriteBuffer;
	}
}
