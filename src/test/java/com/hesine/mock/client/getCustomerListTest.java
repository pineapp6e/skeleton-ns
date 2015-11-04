
package com.hesine.mock.client;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.util.HttpClientUtil;

public class getCustomerListTest {

    public static void main(String[] args) throws Exception {
//        String url = "http://172.27.244.62:8082";
    	String url = "http://localhost:8080";
        notification(url);
    }

    private static void notification(String url) throws Exception {
        Base request = new Base();
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setActionId(ActionInfo.ACTION_ID_GET_MEMBER_LIST);
        actionInfo.setUserId("999");
        //actionInfo.setChatId(1000);
        actionInfo.setUserSource(ActionInfo.ACTION_USRER_SRC_MOBILE);
        actionInfo.setUserType(ActionInfo.ACTION_USRER_TYPE_COMMON_USER);
        //actionInfo.setAppKey("appdemo#demo");
        //actionInfo.setUserType(ActionInfo.ACTION_USRER_TYPE_CUSTOMER);
        request.setActionInfo(actionInfo);
        
        String postData = JSON.toJSONString(request);
        
        postData = "{\"actionInfo\":{\"actionId\":308,\"appKey\":\"DEMO_LILING_KEY\",\"userId\":\"liling@hesine.com\",\"userSource\":1,\"userType\":1}}";
        String response = HttpClientUtil.doPostJson(url, postData);
        if (response != null) {
            System.out.println("response : " + response);
        } else {
            System.out.println("response == null");
        }
    }

}
