package com.hesine.hichat.access.model;

import com.hesine.hichat.model.ReqParam;
import com.hesine.hichat.model.request.Base;

public class NotifyRequest extends Base {
    /** 
     * reqParam:请求参数
     */  
    private ReqParam reqParam;
    /** 
     * notifyType:通知类型
     */  
    private String notifyType;
    /** 
     * message:内容
     */  
    private String message;

    public ReqParam getReqParam() {
        return reqParam;
    }

    public void setReqParam(ReqParam reqParam) {
        this.reqParam = reqParam;
    }

    public String getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
