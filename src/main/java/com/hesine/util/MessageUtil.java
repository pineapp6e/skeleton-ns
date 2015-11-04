package com.hesine.util;

import static com.hesine.hichat.access.handler.ResponseGenerator.sendHttpResponse;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.model.ChatOperationRequest;
import com.hesine.hichat.access.model.ChatOperationResponse;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.DispatchChatInfo;
import com.hesine.hichat.model.response.Base;
import com.hesine.hichat.model.response.JoinNoticeMsg;

public class MessageUtil {
	private static Logger logger = Logger.getLogger(MessageUtil.class);

	public static String getMessageId(ActionInfo actionInfo) {
		return actionInfo.getAppKey() + "-" + actionInfo.getUserId() + "-"
				+ System.currentTimeMillis();
	}

	public static long generateChatId() {
		int randNumber = getRandomNumber(999, 100);
		String chatId = new Long(System.currentTimeMillis()).toString()
				+ new Integer(randNumber).toString();
		return Long.parseLong(chatId);
	}
	
	public static String generateServiceKey(String appKey, String userId){
		return appKey+"-"+userId;
	}

	public static int getRandomNumber(int max, int min) {
		if (max <= min) {
			return min;
		}
		if (max < 0 || min < 0) {
			return 0;
		}
		Random random = new Random();
		int s = random.nextInt(max) % (max - min + 1) + min;
		return s;
	}

	public static ChatOperationResponse getChatOperationResponse(int ret,
			ChatOperationRequest msg) {
		ChatOperationResponse response = new ChatOperationResponse();
		response.setActionId(msg.getActionInfo().getActionId());
		String userId = msg.getActionInfo().getUserId();
		if (userId != null && !userId.isEmpty()) {
			response.setUserAccount(userId);
		}
		response.setCode(ret);
		if (ret == 0) {
			response.setMessage("success");
		} else {
			response.setMessage("failure");
		}

		return response;
	}

	public static Base getSimpleResponse(int ret, int actionId) {
		Base response = new Base();
		response.setActionId(actionId);
		response.setCode(ret);
		if (ret == 0) {
			response.setMessage("success");
		} else {
			response.setMessage("failure");
		}

		return response;
	}

	public static void errorResponse(ChannelHandlerContext ctx, int actionId,
			String errorDesc) {
		Base response = new Base();
		response.setActionId(actionId);
		response.setCode(1);
		response.setMessage(errorDesc);
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
				Unpooled.wrappedBuffer(JSON.toJSONBytes(response)));
		sendHttpResponse(ctx, res);
		logger.info("actionId: " + actionId + ",error Response : "
				+ JSON.toJSONString(response));
	}

	public static JoinNoticeMsg getJoinNotice(DispatchChatInfo dci, int actionId) {
		JoinNoticeMsg jnm = new JoinNoticeMsg();
		jnm.setActionId(actionId);
		List<DispatchChatInfo> newChatList = new ArrayList<DispatchChatInfo>();
		newChatList.add(dci);
		jnm.setNewChatList(newChatList);
		return jnm;
	}

	public static JoinNoticeMsg getJoinNotice(
			Queue<DispatchChatInfo> waitQueue, int actionId) {
		JoinNoticeMsg jnm = new JoinNoticeMsg();
		jnm.setActionId(actionId);
		jnm.setNewChatList(waitQueue);
		return jnm;
	}

}
