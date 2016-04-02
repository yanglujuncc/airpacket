/**
 *  @author hzyanglujun
 *  @version  创建时间:2016年3月18日 下午1:08:19
 */
package org.ylj.airpacket.nio;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * @author hzyanglujun
 *
 */
public class EventReactor {

	private static final Logger logger = Logger.getLogger(EventReactor.class);

	private int selectWaitTime = 3000;
	private Selector selector;
	private EventSelectThread eventSelectThread;

	protected Executor eventHandleExecutor;

	protected ConcurrentLinkedQueue<SelectionKey> toCancelKeys = new ConcurrentLinkedQueue<SelectionKey>();

	public EventReactor() {
		this.eventHandleExecutor = null;
	}

	public EventReactor(Executor eventHandleExecutor) {
		this.eventHandleExecutor = eventHandleExecutor;
	}

	/*
	 * public Selector getSelector(){ if(selector==null){ throw new
	 * RuntimeException("select not inited ."); } return selector; }
	 */
	public int getEventInterestOps(SelectableChannel channel) throws IOException {

		if (channel.isRegistered()) {
			SelectionKey selectionKey = channel.keyFor(selector);
			return selectionKey.interestOps();
		} else {
			return 0;
		}

	}

	public void setEventInterestOps(SelectableChannel channel, int interestOp) throws Exception {

		if (channel.isRegistered()) {
			SelectionKey selectionKey = channel.keyFor(selector);
			int existsOps = selectionKey.interestOps();
			selectionKey.interestOps(interestOp | existsOps);
		} else {
			throw new Exception("channel not Registered()");
		}
	}

	public void unsetEventInterestOps(SelectableChannel channel, int interestOp) throws Exception {

		if (channel.isRegistered()) {
			SelectionKey selectionKey = channel.keyFor(selector);
			int existsOps = selectionKey.interestOps();
			selectionKey.interestOps((~interestOp) & existsOps);
		} else {
			throw new Exception("channel not Registered()");
		}
	}

	public void newEventInterestOps(SelectableChannel channel, int interestOp) throws Exception {

		if (channel.isRegistered()) {
			SelectionKey selectionKey = channel.keyFor(selector);
			selectionKey.interestOps(interestOp);
		} else {
			throw new Exception("channel not Registered()");
		}

	}

	public void registerEventInterestOps(SelectableChannel channel, int interestOp, EventHandler callback) {

		try {
			channel.register(selector, interestOp, callback);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}

	}

	public void unRegisterEventInterestOps(SelectableChannel channel) {
		logger.info("channel.isRegistered():" + channel.isRegistered());

		if (channel.isRegistered()) {
			SelectionKey selectionKey = channel.keyFor(selector);
			toCancelKeys.add(selectionKey);
		}
	}

	public void start() throws IOException {

		start("event");
	}

	public void start(String threadName) throws IOException {

		// open selector.
		selector = Selector.open();

		eventSelectThread = new EventSelectThread();
		eventSelectThread.setName(threadName);
		eventSelectThread.start();
	}

	public void stop() throws IOException {

		eventSelectThread.interrupt();
		// 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("", e);
			}
		}
	}

	class EventSelectThread extends Thread {
		@Override
		public void run() {

			eventSelectLoop();

		}
	}

	public void eventSelectLoop() {
		int loop = 0;
		while (true) {

			loop++;

			if (Thread.interrupted()) {
				logger.info("interrupted true");
				break;
			}

			try {
				selector.select(selectWaitTime);
			} catch (Exception e) {
				logger.error("selector.select(" + selectWaitTime + ")", e);
				break;
			}
			Set<SelectionKey> selectesKeys = selector.selectedKeys();
			int allKeysCountetr = selector.keys().size();
			int selectedKeysCounter = selectesKeys.size();
			int toCancelKeysCounter = toCancelKeys.size();

			logger.info("client select loop(" + loop + ")  selectedKeys:" + selectedKeysCounter + "/" + allKeysCountetr + " toCancel:" + toCancelKeysCounter);

			Iterator<SelectionKey> it = selectesKeys.iterator();
			SelectionKey key = null;
			while (it.hasNext()) {

				key = it.next();
				it.remove();

				if (eventHandleExecutor != null) {
					handleEventByExecutor(key);
				} else {
					handleEvent(key);
				}

			}

			/**
			 * do cancel key
			 */

			for (SelectionKey toCancelKey : toCancelKeys) {
				toCancelKey.cancel();
			}
			toCancelKeys.clear();

		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Runnable#run()
	 */

	private void handleEvent(SelectionKey key) {
		EventHandler eventHandler = (EventHandler) key.attachment();
		if (key.isValid()) {
			// 判断是否连接成功

			if (key.isAcceptable()) {
				logger.info(" key.isAcceptable() ");
				eventHandler.doAcceptable();
			}
			if (key.isConnectable()) {
				logger.info(" key.isConnectable() ");
				eventHandler.doConnectable();
			}
			if (key.isReadable()) {
				logger.info(" key.isReadable() ");
				eventHandler.doReadable();
			}
			if (key.isWritable()) {
				logger.info(" key.isWritable() ");
				eventHandler.doWritable();
			}

		} else {
			logger.info(" key.isNotValid() ");
			eventHandler.doNotValid();
		}

	}

	private void handleEventByExecutor(SelectionKey key) {
		final EventHandler eventHandler = (EventHandler) key.attachment();
		try {
			if (key.isValid()) {
				// 判断是否连接成功

				if (key.isAcceptable()) {
					logger.info(" key.isAcceptable() ");
					eventHandleExecutor.execute(new Runnable() {
						@Override
						public void run() {
							eventHandler.doAcceptable();
						}
					});
				}
				if (key.isConnectable()) {
					logger.info(" key.isConnectable() ");
					eventHandleExecutor.execute(new Runnable() {
						@Override
						public void run() {
							eventHandler.doConnectable();
						}
					});
				}
				if (key.isReadable()) {
					logger.info(" key.isReadable() ");
					eventHandleExecutor.execute(new Runnable() {
						@Override
						public void run() {
							eventHandler.doReadable();
						}
					});
				}
				if (key.isWritable()) {
					logger.info(" key.isWritable() ");
					eventHandleExecutor.execute(new Runnable() {
						@Override
						public void run() {
							eventHandler.doWritable();
						}
					});
				}
			} else {
				logger.info(" key.isNotValid() ");

				eventHandleExecutor.execute(new Runnable() {
					@Override
					public void run() {
						eventHandler.doNotValid();
					}
				});
			}
		} catch (CancelledKeyException canceledKey) {
			logger.info(" canceledKey ... ");
		}

	}

	public boolean isRegisteredEvent(SelectableChannel channel) {
		return channel.isRegistered();
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws ClosedChannelException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws ClosedChannelException, IOException, InterruptedException {

		DOMConfigurator.configure("conf/log4j.xml");

		EventReactor client = new EventReactor();
		client.start();

	}

}
