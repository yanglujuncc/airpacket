/**
 *  @author hzyanglujun
 *  @version  创建时间:2016年3月18日 下午2:40:31
 */
package ylj.demo.network.nio;

import java.nio.channels.SelectionKey;

/**
 * @author hzyanglujun
 *
 */
public class NIODemo {

	public static void main(String[] args){
		System.out.println("SelectionKey.OP_READ:"+SelectionKey.OP_READ);
		System.out.println("SelectionKey.OP_WRITE:"+SelectionKey.OP_WRITE);
		System.out.println("SelectionKey.OP_ACCEPT:"+SelectionKey.OP_ACCEPT);
		System.out.println("SelectionKey.OP_CONNECT:"+SelectionKey.OP_CONNECT);
	
	}
}
