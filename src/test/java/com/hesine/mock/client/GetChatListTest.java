
package com.hesine.mock.client;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.ReqParam;
import com.hesine.hichat.model.request.ChatList;
import com.hesine.util.HttpClientUtil;

public class GetChatListTest {

    public static void main(String[] args) throws Exception {
        String url = "http://localhost:8080";
        notification(url);
    }

    private static void notification(String url) throws Exception {
        ChatList chatListRequest = new ChatList();
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setActionId(ActionInfo.ACTION_ID_CHAT_LIST);
        actionInfo.setUserId("service1@hesine.com");
        actionInfo.setUserSource(ActionInfo.ACTION_USRER_TYPE_CUSTOMER);
        actionInfo.setUserType(1);
        actionInfo.setAppKey("MS_DEMO_KEY");
        chatListRequest.setActionInfo(actionInfo);
        
        ReqParam reqParam = new ReqParam();
//        reqParam.setHospitalId("1");
//        reqParam.setHospitalIds("1,2");
//        reqParam.setDoctorIds("1019613,368");
        //reqParam.setUserIds("368");
       // reqParam.setDataFids("chatlastmsg");
        
        chatListRequest.setChatParam(reqParam);
        reqParam.setChatFlag(0);
        reqParam.setChatType(0);
        reqParam.setCurPage(1);
        reqParam.setCustomerNickName("");
        reqParam.setPageSize(10);
        reqParam.setUserId("service1@hesine.com");
        String postData = JSON.toJSONString(chatListRequest);
//        postData = "{\"actionInfo\":{\"actionId\":309,\"appKey\":\"HICHAT_TEST_KEY\",\"userId\":\"admin1@hesine.com\",\"userSource\":1,\"userType\":1},\"chatParam\":{\"chatFlag\":1,\"chatType\":0}}";
        postData = "{\"actionInfo\":{\"actionId\":309,\"appKey\":\"DEMO_LILING_KEY\",\"userId\":\"liling@hesine.com\",\"userSource\":1,\"userType\":1},\"chatParam\":{\"chatFlag\":0,\"chatType\":0,\"curPage\":1,\"pageSize\":10,\"userId\":\"liling@hesine.com\"}}";
        System.out.println(postData);
        
        String response = HttpClientUtil.doPostJson(url, postData);
        if (response != null) {
            System.out.println("response : " + response);
        } else {
            System.out.println("response == null");
        }
    }

}
