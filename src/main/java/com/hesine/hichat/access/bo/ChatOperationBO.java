
package com.hesine.hichat.access.bo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hesine.hichat.access.model.ChatOperationRequest;
import com.hesine.hichat.access.model.ChatStatus;
import com.hesine.hichat.model.AccountInfo;
import com.hesine.hichat.model.ChatInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.hichat.model.request.SendMsg;
import com.hesine.hichat.model.response.CustomerInfo;

public interface ChatOperationBO {

	ChatStatus createChat(SendMsg msg);
	
	public List<CustomerInfo> getCustomerList(Base msg);
	public ChatInfo getChatStatusInfo(String userId,String serviceNumber);
	AccountInfo getAccountInfo(String appKey,String userAccount);

    int joinChat(ChatOperationRequest cor);

    int kickChat(ChatOperationRequest cor);

    int transferChat(ChatOperationRequest cor);

    int closeChat(Base base);
    
    int inchatCnt(String csAccount);
    
    Map<String, Set<String>> dayChatPersonAppKey(boolean filterByPerson);

	String getCustomerAccount(Long chatId, String userId);

}
