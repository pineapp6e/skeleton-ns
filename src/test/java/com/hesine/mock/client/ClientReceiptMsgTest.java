
package com.hesine.mock.client;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.ReceiptMsg;
import com.hesine.util.HttpClientUtil;

public class ClientReceiptMsgTest {

    public static void main(String[] args) throws Exception {
        //String url = "http://211.151.62.38:8080";
    	String url = "http://localhost:8080";
        notification(url);
    }

    private static void notification(String url) throws Exception {
    	ReceiptMsg receiptMsg = new ReceiptMsg();
                
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setActionId(ActionInfo.ACTION_ID_RECEIPT_MSG);
        actionInfo.setUserId("999");
        //actionInfo.setChatId(1000);
        actionInfo.setUserSource(ActionInfo.ACTION_USRER_SRC_MOBILE);
        actionInfo.setUserType(ActionInfo.ACTION_USRER_TYPE_COMMON_USER);
        receiptMsg.setActionInfo(actionInfo);
        
        List<String> recvMsgList = new ArrayList<String>();
        recvMsgList.add("customer1-1437547081797");
        recvMsgList.add("customer1-1437725901750");
        receiptMsg.setRecvMsgList(recvMsgList);
        
        String postData = JSON.toJSONString(receiptMsg);
        
        String response = HttpClientUtil.doPostJson(url, postData);
        if (response != null) {
            System.out.println("response : " + response);
        } else {
            System.out.println("response == null");
        }
    }

}
