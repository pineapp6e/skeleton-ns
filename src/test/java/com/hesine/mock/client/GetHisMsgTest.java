
package com.hesine.mock.client;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.HisMsgParam;
import com.hesine.hichat.model.request.GetHisMsg;
import com.hesine.util.HttpClientUtil;

public class GetHisMsgTest {

    public static void main(String[] args) throws Exception {
        String url = "http://localhost:8080";
//    	String url = "http://172.27.244.62:8082";
        notification(url);
    }

    private static void notification(String url) throws Exception {
        GetHisMsg request = new GetHisMsg();
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setActionId(ActionInfo.ACTION_ID_GET_HIS_MSG);
        actionInfo.setUserId("test@hesine.com");
        actionInfo.setAppKey("HICHAT_TEST_KEY");
        
        actionInfo.setUserSource(ActionInfo.ACTION_USRER_SRC_MOBILE);
        request.setActionInfo(actionInfo);
        
        HisMsgParam hmp = new HisMsgParam();
//        hmp.setChatId(1000);
//        hmp.setLimit(2);    
        hmp.setChatId(1439969446529739L);
        hmp.setDestUserId("serviceSupport");
        request.setHisMsgParam(hmp);
        
        String postData = JSON.toJSONString(request);
        
        
        
        postData = "{\"actionInfo\":{\"actionId\":307,\"appKey\":\"HICHAT_TEST_KEY\",\"userId\":\"chu@123.com\",\"userSource\":1,\"userType\":1},\"hisMsgParam\":{\"chatId\":1443149901026407,\"destUserId\":\"\",\"limit\":30,\"offset\":-1,\"order\":1,\"time\":0}}";
        String response = HttpClientUtil.doPostJson(url, postData);
        if (response != null) {
            System.out.println("response : " + response);
        } else {
            System.out.println("response == null");
        }
    }

}


