package com.hesine.mock.client;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.model.ChatOperationRequest;
import com.hesine.hichat.access.model.ChatUserInfo;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.ReqParam;
import com.hesine.util.HttpClientUtil;


public class ChatOperationTest {

    public static void main(String[] args) throws Exception {
        String url = "http://localhost:8080";
//         int chatOperation = ActionInfo.ACTION_ID_ADD_USER;
//         int chatOperation = ActionInfo.ACTION_ID_KICK_USER;
         int chatOperation = ActionInfo.ACTION_ID_CHANGE_USER;
        // int chatOperation = ActionInfo.ACTION_ID_CLOSE_CHAT;
//        int chatOperation = ActionInfo.ACTION_ID_CREATE_CHAT;
        createChat(url, chatOperation);
    }

    private static void createChat(String url, int chatOperation) throws Exception {
        ChatOperationRequest chatOperationRequest = new ChatOperationRequest();
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setActionId(chatOperation);
        actionInfo.setUserId("999");
        actionInfo.setUserSource(ActionInfo.ACTION_USRER_TYPE_CUSTOMER);
        chatOperationRequest.setActionInfo(actionInfo);

        ChatUserInfo chatUserInfo = new ChatUserInfo();
        chatUserInfo.setChatId(2000);
        chatUserInfo.setDoctorId("9999");
//        chatUserInfo.setUserId("111");
        chatUserInfo.setHospitalId("666");
        chatOperationRequest.setChatUserInfo(chatUserInfo);

        if (chatOperation == ActionInfo.ACTION_ID_CLOSE_CHAT) {
            ReqParam reqParam = new ReqParam();
           // reqParam.setHospitalId("1");
            chatOperationRequest.setReqParam(reqParam);
        }

        String postData = JSON.toJSONString(chatOperationRequest);
        System.out.println("createChat: "+ postData);
        String response = HttpClientUtil.doPostJson(url, postData);
        if (response != null) {
            System.out.println("response : " + response);
        } else {
            System.out.println("response == null");
        }
    }

}
