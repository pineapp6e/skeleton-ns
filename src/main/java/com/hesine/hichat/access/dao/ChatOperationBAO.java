package com.hesine.hichat.access.dao;


import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hesine.hichat.access.model.SettingInfo;
import com.hesine.hichat.model.ChatInfo;
import com.hesine.hichat.model.CsChatSummary;
import com.hesine.hichat.model.DispatchChatInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.hichat.model.request.SendMsg;
import com.hesine.hichat.model.response.CustomerInfo;

public interface ChatOperationBAO {

    int checkChatUser(long chatId, String userId);
    String getOtherChatUser(long chatId, String userId);
    
    int insertChatUser(long chatId, String account);
    long createChat(SendMsg msg,long chatId,String userAccount,String virtualNumber, int status);
    
    ChatInfo getChatStatusInfo(String userId,String serviceNumber);
    List<CustomerInfo> getCustomerList(Base msg);

    int checkAuthAccount(String account);

    int insertAuthAccount(String account, int userType);

    int deleteChatUser(long chatId, String account);

    int updateChatStatus(long chatId, int chatStatus);

    int updateChatUser(long chatId, String oldAccount, String newAccount);

    int checkUserExtend(String account, String groupId);

    int insertUserExtend(String account, String groupId);

    int checkGroup(String hospitalId);

    int insertGroup(String hospitalId);
    
    Map<Integer,SettingInfo> getSettingInfo(String appKey); 
    
	void createChat(String userId, String appKey, long chatId,
			String serviceNumber, int chatStatus);

	int getInchatCnt(String csAccount);
	
	Map<String,List<DispatchChatInfo>> getDispatchChats();

	Map<String, Set<String>> getChatPersonMapByAppkey(boolean filterByPerson);

	Map<? extends String, ? extends Integer> getMessageCntDay();

	Map<? extends String, ? extends Map<String, CsChatSummary>> getCsStatisticDay();

}
