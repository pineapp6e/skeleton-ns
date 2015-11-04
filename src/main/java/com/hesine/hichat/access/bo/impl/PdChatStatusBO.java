/**
 * 
 */
package com.hesine.hichat.access.bo.impl;

import static com.hesine.hichat.access.common.EnumConstants.HICHAT_ERROR_USER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.bo.ChatStatusBO;
import com.hesine.hichat.access.dao.ChatListDAO;
import com.hesine.hichat.model.ChatInfo;
import com.hesine.hichat.model.ReqParam;
import com.hesine.hichat.model.response.ChatList;
import com.hesine.util.AccountUtil;

/**
 * @author pineapple
 * 
 */
@Component("chatStatusBO")
public class PdChatStatusBO implements ChatStatusBO {
	
	@Autowired
	private ChatListDAO chatListDAO;
	
	@Override
	public ChatList getChatList(com.hesine.hichat.model.request.ChatList chatList) {
		String account = AccountUtil.getRequestAccount(chatList);
		ReqParam reqParam = chatList.getChatParam();
		ChatList cl = new ChatList();

		if (account == null || account.isEmpty()) {
			cl.setCode(HICHAT_ERROR_USER);
			return cl;
		}		

		cl.setUserAccount(account);		
		List<ChatInfo> csiList = chatListDAO.getChatList(chatList.getActionInfo().getAppKey(), reqParam);
		cl.setTotalChat(chatListDAO.getHisChatCnt(chatList.getActionInfo().getAppKey(), reqParam));
		cl.setCode(0);
		cl.setChatList(csiList);
		return cl;
	}


}
