package com.hesine.hichat.access.model;

import com.hesine.hichat.model.ReqParam;
import com.hesine.hichat.model.request.Base;

public class ChatOperationRequest extends Base {
    /** 
     * chatUserInfo:对话操作信息
     */  
    private ChatUserInfo chatUserInfo;
    /** 
     * reqParam:通知医生条件
     * 关闭对话是有作用
     */  
    private ReqParam reqParam;

    public ChatUserInfo getChatUserInfo() {
        return chatUserInfo;
    }

    public void setChatUserInfo(ChatUserInfo chatUserInfo) {
        this.chatUserInfo = chatUserInfo;
    }

    public ReqParam getReqParam() {
        return reqParam;
    }

    public void setReqParam(ReqParam reqParam) {
        this.reqParam = reqParam;
    }

}
