
package com.hesine.mock.client;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.ReqParam;
import com.hesine.hichat.model.request.GetUserInfo;
import com.hesine.util.HttpClientUtil;

public class getAccountInfoTest {

    public static void main(String[] args) throws Exception {
    	String url = "http://172.27.244.62:8082";
    	//String url = "http://localhost:8080";
        notification(url);
    }

    private static void notification(String url) throws Exception {
    	GetUserInfo request = new GetUserInfo();
    	ReqParam userParam = new ReqParam();
    	userParam.setUserId("hichat_test");
    	request.setUserParam(userParam);
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setActionId(ActionInfo.ACTION_ID_USER_INFO);
        actionInfo.setUserId("test@hesine.com");                
        actionInfo.setAppKey("HICHAT_TEST_KEY");        
        request.setActionInfo(actionInfo);
        
        String postData = JSON.toJSONString(request);
        
        String response = HttpClientUtil.doPostJson(url, postData);
        if (response != null) {
            System.out.println("response : " + response);
        } else {
            System.out.println("response == null");
        }
    }

}
