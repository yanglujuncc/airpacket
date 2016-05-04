package org.ylj.message.mqtt;

import com.alibaba.fastjson.JSON;

public class CoreMsgMQTTTopicName {

	/*
	 * //上行upTopic let UpTopicName = "clients/01/up" //下行 downTopic let
	 * DownTopicName = "clients/01/down"
	 * 
	 */
	static final String Flag_Clients = "clients";
	static final String Flag_UpDown_Up = "up";
	static final String Flag_UpDown_Down = "down";

	static final int Up = 1;
	static final int Down = 2;

	public int type;// down=1 up=2
	public String userId;

	public CoreMsgMQTTTopicName() {

	}

	public CoreMsgMQTTTopicName(int type, String userId) {
		this.type = type;
		this.userId = userId;
	}

	public String toTopicNameStr() {

		if (userId == null)
			return null;

		if (Up == type) {
			return Flag_Clients + "/" + userId + "/" + Flag_UpDown_Up;
		}
		if (Down == type) {
			return Flag_Clients + "/" + userId + "/" + Flag_UpDown_Down;
		}

		return null;
	}

	public static CoreMsgMQTTTopicName parse(String topicNameStr) {

		int idxFirstFlag = topicNameStr.indexOf('/');
		// System.out.println("idxFirstFlag:"+idxFirstFlag);
		if (idxFirstFlag == -1)
			return null;
		int idxSecondFlag = topicNameStr.indexOf('/', idxFirstFlag + 1);
		// System.out.println("idxSecondFlag:"+idxSecondFlag);
		if (idxSecondFlag == -1)
			return null;

		String clientsFlag = topicNameStr.substring(0, idxFirstFlag);
		String userId = topicNameStr.substring(idxFirstFlag + 1, idxSecondFlag);
		String updownFlag = topicNameStr.substring(idxSecondFlag + 1);

		// System.out.println("clientsFlag:"+clientsFlag);
		// System.out.println("userId:"+userId);
		// System.out.println("updownFlag:"+updownFlag);

		if (!Flag_Clients.equals(clientsFlag)) {
			return null;
		}
		if (!Flag_UpDown_Up.equals(updownFlag) && !Flag_UpDown_Down.equals(updownFlag)) {
			return null;
		}

		CoreMsgMQTTTopicName coreMsgTopicName = new CoreMsgMQTTTopicName();
		if (Flag_UpDown_Up.equals(updownFlag)) {
			coreMsgTopicName.type = Up;
		} else if (Flag_UpDown_Down.equals(updownFlag)) {
			coreMsgTopicName.type = Down;
		}
		coreMsgTopicName.userId = userId;

		return coreMsgTopicName;
	}

	public static void main(String[] args) {
		String UpTopicName = "clients/01/up";
		// 下行 downTopic
		String DownTopicName = "clients/01/down";

		System.out.println(JSON.toJSONString(CoreMsgMQTTTopicName.parse(UpTopicName), true));
		System.out.println(JSON.toJSONString(CoreMsgMQTTTopicName.parse(DownTopicName), true));
		
		CoreMsgMQTTTopicName aCoreMsgTopicName=new CoreMsgMQTTTopicName(CoreMsgMQTTTopicName.Up,"xxx");
		
		System.out.println(aCoreMsgTopicName.toTopicNameStr());
	}
}
