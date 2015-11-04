/**
 * 
 */
package com.hesine.hichat.access.handler;

import static com.hesine.util.PropertiesUtil.getBooleanValue;
import static com.hesine.util.PropertiesUtil.getValue;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.hesine.hichat.access.common.EnumConstants;
import com.hesine.hichat.access.model.AuthAccount;
import com.hesine.hichat.access.service.ClientChannelCache;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.response.Base;
import com.hesine.util.HcpsPushUtil;
import com.hesine.util.MessageUtil;

/**
 * @author pineapple
 *
 */
public class NotifyClientUtil {
	private static final Logger logger = Logger
			.getLogger(NotifyClientUtil.class.getName());

	public static Base buildNotifyData(String userAccount) {
		Base notifyMsg = new Base();
		notifyMsg.setActionId(ActionInfo.ACTION_ID_NEW_MSG_NOTICE);
		return notifyMsg;
	}

	public static void notifyAll(String notifyMsg) {
		if (!CollectionUtils.isEmpty(ClientChannelCache.getCsGroup())) {
			for (Entry<String, ChannelGroup> entry : ClientChannelCache
					.getCsGroup().entrySet()) {
				notifyGroup(notifyMsg, entry.getKey(), null);
			}
		}
	}

	public static void notifyGroup(final String notifyMsg,
			String serviceGroupId, final List<String> customerList) {
		ChannelGroup recipients = ClientChannelCache.getGroup(serviceGroupId);
		if (recipients == null) {
			logger.warn(" no customer service online in this customer group!");
		} else {
			logger.info("notice customer list:" + customerList);
			recipients.flushAndWrite(new TextWebSocketFrame(notifyMsg),
					new ChannelMatcher() {
						@Override
						public boolean matches(Channel channel) {
							logger.info("channel's client key:"
									+ channel.attr(
											WebSocketServerHandler.CLIENT_KEY)
											.get());
							logger.info("actionId:" + notifyMsg);
							return CollectionUtils.isEmpty(customerList) ? true
									: customerList.contains(channel.attr(
											WebSocketServerHandler.CLIENT_KEY)
											.get());
						}
					});
		}
	}

	public static void notifyBySocket(String notifyMsg, String key) {
		Channel channel = ClientChannelCache.getClient(key);
		if (channel != null) {
			logger.info("notify new msg via web socket:" + notifyMsg);
			channel.writeAndFlush(new TextWebSocketFrame(notifyMsg));
		} else {
			logger.info("account " + key + " not connect socket");
		}
	}

	public static void notifyMobile(AuthAccount mobileUser) {
		if (mobileUser == null) {
			return;
		}
		// to PN hichat_dev
		final String pnToken = mobileUser.getPnToken();
		final String hcpsUrl = getValue("hcps.url");
		final boolean isBasic = getBooleanValue("hcps.auth.basic");
		final String payload = "8888801";
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = null;
				try {
					result = HcpsPushUtil.push(hcpsUrl + "/message/push"
							+ (isBasic ? "/basic" : ""),
							getValue("hcps.account"), getValue("hcps.pwd"),
							unicastData(payload, pnToken));
				} catch (IOException e) {
					logger.error("request hcps error, result is " + result, e);
					// 此异常不应出现。
				}
			}
		}).start();
	}

	private static Map<String, Object> unicastData(String payload, String token) {
		/**
		 * add push data to map.
		 */
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("notifType", EnumConstants.UNICAST);
		paramMap.put("payload", payload);
		paramMap.put("pnType", EnumConstants.HPNS);
		paramMap.put("expiry", EnumConstants.MAX_KEEP_TIME_IN_PN);
		paramMap.put("regId", token);
		return paramMap;
	}

	public static void notifyGroupByRole(final String jsonString,
			final String serviceGroupId, int role) {
		ChannelGroup recipients = ClientChannelCache.getGroup(serviceGroupId);
		if (recipients == null) {
			logger.warn(" no customer service online in this customer group!");
		} else {
			logger.info("actionId:" + jsonString);
			logger.info("notice admins.");
			recipients.flushAndWrite(new TextWebSocketFrame(jsonString),
					new ChannelMatcher() {
						@Override
						public boolean matches(Channel channel) {
							logger.info("channel's client key:"
									+ channel.attr(
											WebSocketServerHandler.CLIENT_KEY)
											.get());
							String userId = channel.attr(
									WebSocketServerHandler.CLIENT_KEY).get();

							return ClientChannelCache.isAdmin(MessageUtil.generateServiceKey(serviceGroupId
									,userId));
						}
					});
		}
	}
}
