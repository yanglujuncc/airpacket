package ylj.demo.network.mqtt.moquette.house;

public class IMMsg {
	public int state;
	public int id;
	public String from;  //clientId
	public String to;    //clientId
	public String content;
	public long sendTimetstamp;
}
