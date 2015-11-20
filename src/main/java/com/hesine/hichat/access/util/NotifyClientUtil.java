/**
 * 
 */
package com.hesine.hichat.access.util;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Date;

import org.apache.log4j.Logger;

import com.hesine.hichat.access.model.ClientChannelMap;

/**
 * @author pineapple
 *
 */
public class NotifyClientUtil {
	private static final Logger logger = Logger
			.getLogger(NotifyClientUtil.class.getName());

	public static void notifyGroup(String notifyMsg, String groupId) {
		ChannelGroup recipients = ClientChannelMap.CLIENT_GROUP_MAP
				.get(groupId);
		if (recipients == null || recipients.isEmpty()) {
			logger.warn(" no client online in this group!");
		} else {
			logger.info("start time: " + new Date(System.currentTimeMillis())
					+ " send clients number:" + recipients.size());
			ChannelGroupFuture groupFuture = recipients
					.flushAndWrite(new TextWebSocketFrame(notifyMsg));
			groupFuture.awaitUninterruptibly();
			if (groupFuture.isSuccess()) {
				logger.info("push to all success");
			} else {
				logger.info("push to all failed");
			}
		}
	}

}
