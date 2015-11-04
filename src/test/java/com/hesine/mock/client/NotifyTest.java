
package com.hesine.mock.client;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.model.NotifyRequest;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.ReqParam;
import com.hesine.util.HttpClientUtil;

public class NotifyTest {

    public static void main(String[] args) throws Exception {
        String url = "http://localhost:8080";
        notification(url);
    }

    private static void notification(String url) throws Exception {
        NotifyRequest notifyRequest = new NotifyRequest();
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setActionId(ActionInfo.ACTION_ID_NOTIFY);
        actionInfo.setUserId("368");
        actionInfo.setUserSource(ActionInfo.ACTION_USRER_TYPE_CUSTOMER);
        ReqParam reqParam = new ReqParam();
//        reqParam.setHospitalId("1");
        //reqParam.setDoctorIds("9999");
//        reqParam.setHospitalIds("1,666");
        
        notifyRequest.setActionInfo(actionInfo);
        notifyRequest.setReqParam(reqParam);
        notifyRequest.setNotifyType("1");
        notifyRequest.setMessage("abcd");
        
        String postData = JSON.toJSONString(notifyRequest);
        
        String response = HttpClientUtil.doPostJson(url, postData);
        if (response != null) {
            System.out.println("response : " + response);
        } else {
            System.out.println("response == null");
        }
    }

}
