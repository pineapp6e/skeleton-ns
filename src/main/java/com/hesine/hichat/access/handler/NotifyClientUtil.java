/**
 * 
 */
package com.hesine.hichat.access.handler;

import static com.hesine.util.PropertiesUtil.getBooleanValue;
import static com.hesine.util.PropertiesUtil.getValue;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hesine.hichat.access.common.EnumConstants;
import com.hesine.hichat.access.model.AuthAccount;
import com.hesine.hichat.access.model.ClientChannelMap;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.response.Base;
import com.hesine.util.HcpsPushUtil;

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

	
	public static void notifyGroup(String notifyMsg, String groupId) {
		ChannelGroup recipients = ClientChannelMap.CLIENT_GROUP_MAP.get(groupId);
		if (recipients == null || recipients.isEmpty()) {
			logger.warn(" no client online in this group!");
		} else {
			recipients.flushAndWrite(new TextWebSocketFrame(notifyMsg));
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

}
