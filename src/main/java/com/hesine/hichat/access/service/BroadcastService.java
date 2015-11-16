/**
 * 
 */
package com.hesine.hichat.access.service;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.handler.NotifyClientUtil;
import com.hesine.hichat.access.model.ClientChannelMap;

/**
 * @author wanghua
 *
 */
@Component
public class BroadcastService {
    private static Logger logger = Logger.getLogger(BroadcastService.class
            .getName());
    
    @Scheduled(cron = "20/60 * * * * ?")
    public void broadcastMessages(){
    	logger.info("broadcast to all clients.");
    	NotifyClientUtil.notifyGroup("Hello world!", ClientChannelMap.DEFAULT_GROUP);
    }
    
}
