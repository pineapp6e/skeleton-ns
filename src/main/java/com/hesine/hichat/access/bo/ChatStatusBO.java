package com.hesine.hichat.access.bo;

import com.hesine.hichat.model.request.ChatList;



public interface ChatStatusBO {

	com.hesine.hichat.model.response.ChatList getChatList(ChatList msg);
	
}
