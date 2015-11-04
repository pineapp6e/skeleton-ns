package com.hesine.util;

import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.Base;

public class AccountUtil {

    public static String getRequestAccount(Base request) {
        if (request.getActionInfo().getUserId() != null
                && !request.getActionInfo().getUserId().isEmpty()) {
            return request.getActionInfo().getUserId();
        }        
        return null;
    }
    
    public static String getDeviceToken(ActionInfo actionInfo){
    	String deviceToken = null;
    	if(actionInfo!=null){
    		deviceToken = actionInfo.getDeviceToken();
    		if(deviceToken == null){
    			deviceToken = actionInfo.getUserId();
    		}
    	}
    	return deviceToken;
    }

}
