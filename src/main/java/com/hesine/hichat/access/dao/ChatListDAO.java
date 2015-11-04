
package com.hesine.hichat.access.dao;

import java.util.List;

import com.hesine.hichat.model.ChatInfo;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.ReqParam;

public interface ChatListDAO {

    List<ChatInfo> getChatIdByHospitals(String hospitalIds);

    List<ChatInfo> getChatIdByAccounts(String Accounts);

    int getChatUnreadNum(long chatId);

    int getChatMsgTotal(long chatId);

    MessageInfo getChatLastMsg(long chatId);

    long getChatLastTime(long chatId);

    List<MessageInfo> getLastDaysMsgs(long chatId, int number);	

    List<ChatInfo> getChatList(String appKey, ReqParam reqParam);

	int getHisChatCnt(String appKey, ReqParam reqParam);

}
