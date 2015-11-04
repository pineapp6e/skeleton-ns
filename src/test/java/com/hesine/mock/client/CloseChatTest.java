
package com.hesine.mock.client;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.util.HttpClientUtil;

public class CloseChatTest {

    public static void main(String[] args) throws Exception {
        String url = "http://localhost:8080";
        notification(url);
    }

    private static void notification(String url) throws Exception {
    	Base base = new Base();
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setActionId(ActionInfo.ACTION_ID_CLOSE_CHAT);
        actionInfo.setAppKey("HICHAT_TEST_KEY");
        actionInfo.setUserId("test@hesine.com");
        actionInfo.setUserSource(ActionInfo.ACTION_USRER_TYPE_CUSTOMER);
        actionInfo.setChatId(1439370143728299L);
        base.setActionInfo(actionInfo);
        
        String postData = JSON.toJSONString(base);
        
        String response = HttpClientUtil.doPostJson(url, postData);
        if (response != null) {
            System.out.println("response : " + response);
        } else {
            System.out.println("response == null");
        }
    }

}
