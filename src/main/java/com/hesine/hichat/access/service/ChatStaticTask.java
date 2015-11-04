/**
 * 
 */
package com.hesine.hichat.access.service;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.bo.ChatOperationBO;
import com.hesine.hichat.model.CsChatSummary;

/**
 * @author pineapple
 *
 */
@Component
public class ChatStaticTask {
	private static Logger logger = Logger.getLogger(ChatStaticTask.class);
	@Autowired
	private ChatOperationBO chatOperationBO;

	/**
	 * 当天会话加载
	 */
	@Scheduled(cron = "0 0 * * * ? ")
	public void dayChatLoad() {
		ClientChannelCache.DAY_CHAT_NUMBER.clear();
		ClientChannelCache.DAY_CHAT_PERSON_NUMBER.clear();
		ClientChannelCache.DAY_MESSAGES_NUMBER.clear();
		
		for(Entry<String,Map<String,  CsChatSummary>> entry: ClientChannelCache.DAY_RECEPTION_STATISTIC.entrySet() ){
			for(CsChatSummary ccs : entry.getValue().values()){
				ccs.clear();
			}
		}
		logger.info("reload day_chat_number map");
	}

}
