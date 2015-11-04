/**
 * 
 */
package com.hesine.mock.client;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.AttachInfo;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.request.SendMsg;
import com.hesine.util.HttpClientUtil;

/**
 * @author pineapple
 *
 */
public class SendMessageTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		sendText();
//		sendImage();
//		sendVideo();
//		sendAudio();
	}

	private static void sendText() throws IOException {
		SendMsg sendMsg = new SendMsg();
		ActionInfo actionInfo = new ActionInfo();
		actionInfo.setActionId(ActionInfo.ACTION_ID_SEND_MSG);
		actionInfo.setUserId("wanghua");	
		actionInfo.setAppKey("HICHAT_TEST_KEY");
		actionInfo.setUserType(ActionInfo.ACTION_USRER_TYPE_COMMON_USER);
		sendMsg.setActionInfo(actionInfo);
		MessageInfo messageInfo = new MessageInfo();
		messageInfo.setAttachmentMark(false);
		
		messageInfo.setChatId(1439969446529739L);
		messageInfo.setBody("测试消息11");
		//messageInfo.setTime(time);
		messageInfo.setFrom("wanghua");
		messageInfo.setTo("Hesine service");
		messageInfo.setType(MessageInfo.TYPE_COMMON_USER_AND_CUSTOMER);
		sendMsg.setMessageInfo(messageInfo);

		String postData = JSON.toJSONString(sendMsg);
				
		
		
		 postData = "{\"actionInfo\":{\"actionId\":304,\"appKey\":\"DEMO_LILING_KEY\",\"userId\":\"liling@hesine.com\",\"userSource\":1,\"userType\":1},\"messageInfo\":{\"attachmentMark\":false,\"body\":\"kkk\",\"chatId\":1445576066938192,\"from\":\"liling@hesine.com\",\"source\":1,\"subType\":0,\"time\":0,\"to\":\"liling\",\"type\":0,\"unread\":0}}";
		
		String response = HttpClientUtil.doPostJson("http://localhost:8080", postData);
//		String response = HttpClientUtil.doPostJson("http://172.27.244.62:8082", postData);
		if (response != null) {
			System.out.println("response : " + response);
		} else {
			System.out.println("response == null");
		}
	}
		
	private static void sendVideo() throws IOException {
		SendMsg sendMsg = new SendMsg();
		ActionInfo actionInfo = new ActionInfo();
		actionInfo.setActionId(ActionInfo.ACTION_ID_SEND_MSG);
		actionInfo.setUserId("1019613");
		actionInfo.setUserSource(ActionInfo.ACTION_USRER_SRC_MOBILE);
		sendMsg.setActionInfo(actionInfo);
		MessageInfo messageInfo = new MessageInfo();
		messageInfo.setAttachmentMark(false);
		messageInfo.setBody("test image");
		messageInfo.setChatId(1000L);
		messageInfo.setFrom("1019613");
		messageInfo.setTo("368");
		messageInfo.setType(0);
		messageInfo.setAttachmentMark(true);

		
		AttachInfo attach = new AttachInfo();
		int suffix = (int)(Math.random()*1000);
		attach.setName("test-video-2342"+suffix+".3gp");
		attach.setType(AttachInfo.ATTACH_VIDEO);
		attach.setAttachment(readFile("/usr/local/var/www/hichat/patient1/2015-06-29/test-video.3gp"));			
		messageInfo.setAttachInfo(attach);
		sendMsg.setMessageInfo(messageInfo);
		String postData = JSON.toJSONString(sendMsg);

		String response = HttpClientUtil.doPostJson("http://211.151.62.38:8080", postData);
		if (response != null) {
			System.out.println("response : " + response);
		} else {
			System.out.println("response == null");
		}
	}
	
	private static void sendAudio() throws IOException {
		SendMsg sendMsg = new SendMsg();
		ActionInfo actionInfo = new ActionInfo();
		actionInfo.setActionId(ActionInfo.ACTION_ID_SEND_MSG);
		actionInfo.setUserId("11");
		actionInfo.setUserSource(ActionInfo.ACTION_USRER_SRC_MOBILE);
		sendMsg.setActionInfo(actionInfo);
		MessageInfo messageInfo = new MessageInfo();
		messageInfo.setAttachmentMark(false);
		messageInfo.setBody("test image");
		messageInfo.setChatId(12L);
		messageInfo.setFrom("11");
		messageInfo.setTo("patient1");
		messageInfo.setType(0);
		messageInfo.setAttachmentMark(true);

		
		AttachInfo attach = new AttachInfo();
		attach.setName("test-video-5664"+Math.random()*1000+".amr");
		attach.setType(AttachInfo.ATTACH_AUDIO);
		attach.setAttachment(readFile("/Users/pineapple/Documents/workspace/hichat-access-svn/attachment/patient1/2015-06-29/4_20150629183716.amr"));
		messageInfo.setAttachInfo(attach);
		sendMsg.setMessageInfo(messageInfo);
		String postData = JSON.toJSONString(sendMsg);

		String response = HttpClientUtil.doPostJson("http://localhost:8080", postData);
		if (response != null) {
			System.out.println("response : " + response);
		} else {
			System.out.println("response == null");
		}
	}
	
	private static void sendImage() throws IOException {
		
		SendMsg sendMsg = new SendMsg();
		ActionInfo actionInfo = new ActionInfo();
		actionInfo.setActionId(ActionInfo.ACTION_ID_SEND_MSG);		
		actionInfo.setUserId("hichat_test");	
		actionInfo.setAppKey("HICHAT_TEST_KEY");
		//actionInfo.setUserSource(ActionInfo.ACTION_USRER_SRC_DOCTOR);
		sendMsg.setActionInfo(actionInfo);		
		MessageInfo messageInfo = new MessageInfo();
		messageInfo.setFrom("hichat_test");
		messageInfo.setTo("serviceSupport");
		//messageInfo.setAttachmentMark(false);
		messageInfo.setBody("test image");
		messageInfo.setChatId(1439370143728299L);				
		messageInfo.setType(0);
		messageInfo.setAttachmentMark(true);

		
		AttachInfo attach = new AttachInfo();
//		int suffix = (int)(Math.random()*1000);
		//attach.setName("20150629171255"+suffix+".jpg");
		attach.setName("1.png");
		attach.setType(AttachInfo.ATTACH_PIC);
		//attach.setAttachment(readFile("C:/Users/sangguiyou/Desktop/baisechaokumingchebizhi_428745_11.jpg"));
		attach.setAttachment(readFile("/Users/pineapple/Desktop/cury.gif"));
		//System.out.print(readFile("C:/Users/sangguiyou/Desktop/1.jpg"));
		//attach.setAttachment(AttachFileContext.getThumbnail("/patient1/2015-06-29/", "20150629171255.jpg"));
				
		messageInfo.setAttachInfo(attach);
		sendMsg.setMessageInfo(messageInfo);
		String postData = JSON.toJSONString(sendMsg);
		//System.out.println(postData);
		//System.exit(0);

		String response = HttpClientUtil.doPostJson("http://localhost:8080", postData);
		//String response = HttpClientUtil.doPostJson("http://localhost:8080", postData);
		if (response != null) {
			System.out.println("response : " + response);
		} else {
			System.out.println("response == null");
		}
	}
	
	public static String readFile(String fileName){
		FileInputStream fis;
		//String res = "";
		String inputName = fileName;
		byte[] buffer = null;
		try {
			fis = new FileInputStream(inputName);
			int length = fis.available();
			buffer = new byte[length];
			fis.read(buffer);
			//res = new String(buffer, "ISO-8859-1");			
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return new String(Base64.encodeBase64(buffer));
		//return Utility.base64Encode(res);
	}
	
}
