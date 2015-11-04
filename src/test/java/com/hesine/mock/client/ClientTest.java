package com.hesine.mock.client;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.request.SendMsg;
import com.hesine.util.HttpClientUtil;

public class ClientTest {
	
	private static final Logger logger = Logger.getLogger(ClientTest.class);
	
	public static void main(String[] args) throws Exception {
		String url = "http://localhost:8080";
		int doctorCnt = 1;
		if (args.length == 0) {
			logger.info(" 使用缺省设置: localhost 8080 100（默认在线医生个数编号）");
		} else if (args.length == 3) {
			int port = NumberUtils.toInt(args[1]);
			doctorCnt = Integer.valueOf(args[2]);
			url = "http://"+args[0]+":"+port;
		}else{
			logger.info("用法:");
			logger.info("java -jar 类名 ip 端口  医生端个数（必须为整数）");
			System.exit(0);
		}
		consultDoctor(url,(int) (Math.random() * doctorCnt) + 1);
	}
	
	public static void consultDoctor(String url, int doctorIdx) throws Exception{
		SendMsg baseRequest = new SendMsg();
		ActionInfo actionInfo = new ActionInfo();
		actionInfo.setActionId(ActionInfo.ACTION_ID_SEND_MSG);
		actionInfo.setUserId("customer_"
				+ ((int) (Math.random() * 100) + 1));
		actionInfo.setUserId("doctor_" + doctorIdx);
		MessageInfo message = new MessageInfo();
		message.setBody("how do you do？");
		baseRequest.setMessageInfo(message);
		baseRequest.setActionInfo(actionInfo);
		String postData = JSON.toJSONString(baseRequest);
		
		String response = HttpClientUtil.doPostJson(url, postData);
		logger.info("response : "+ response);
	}
}
