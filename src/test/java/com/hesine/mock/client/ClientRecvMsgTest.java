
package com.hesine.mock.client;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.util.HttpClientUtil;

public class ClientRecvMsgTest {

    public static void main(String[] args) throws Exception {        
    	String url = "http://localhost:8080";
//    	String url = "http://211.151.62.41:8082";
        notification(url);
    }

    private static void notification(String url) throws Exception {
        Base request = new Base();
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setActionId(ActionInfo.ACTION_ID_RECV_MSG);
//        actionInfo.setAppKey("hichat_test");
//        actionInfo.setUserId("hichat_test");
        actionInfo.setAppKey("HICHAT_TEST_KEY");
        actionInfo.setUserId("test@hesine.com");
        //actionInfo.setChatId(1000);
        actionInfo.setUserSource(ActionInfo.ACTION_USRER_SRC_MOBILE);
        actionInfo.setUserType(ActionInfo.ACTION_USRER_TYPE_COMMON_USER);
        request.setActionInfo(actionInfo);
        
        String postData = JSON.toJSONString(request);
        System.out.println(postData);
        
        
        postData = "{\"actionInfo\":{\"actionId\":307,\"appKey\":\"HICHAT_TEST_KEY\",\"userId\":\"chu@123.com\",\"userSource\":1,\"userType\":1},\"hisMsgParam\":{\"chatId\":1443149901026407,\"destUserId\":\"\",\"limit\":30,\"offset\":-1,\"order\":1,\"time\":0}}";
        String response = HttpClientUtil.doPostJson(url, postData);
        if (response != null) {
            System.out.println("response : " + response);
        } else {
            System.out.println("response == null");
        }
    }

}
