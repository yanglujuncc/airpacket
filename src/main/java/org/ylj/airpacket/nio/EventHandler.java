package org.ylj.airpacket.nio;

public interface EventHandler {
	
	public void doNotValid();
	public void doAcceptable();
	public void doConnectable();
	public void doReadable();
	public void doWritable();
	//public boolean cancel(); 
}
