package com.hesine.mock.client;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.Base;

public class TestJson {
	public static void main(String[] args) {
		Base baseRequest = new Base();
		ActionInfo actionInfo = new ActionInfo();
		actionInfo.setActionId(ActionInfo.ACTION_ID_LOGIN_CHAT);
		actionInfo.setUserId("wanghua@hesine.com");
		actionInfo.setPassword("123456");
		baseRequest.setActionInfo(actionInfo);
		System.out.println(JSON.toJSONString(baseRequest));
	}
}
