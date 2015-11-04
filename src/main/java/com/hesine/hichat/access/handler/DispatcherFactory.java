/**
 * 
 */
package com.hesine.hichat.access.handler;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.model.ChatOperationRequest;
import com.hesine.hichat.access.model.DispatchResult;
import com.hesine.hichat.access.model.NotifyRequest;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.hichat.model.request.ChatList;
import com.hesine.hichat.model.request.GetHisMsg;
import com.hesine.hichat.model.request.GetUserInfo;
import com.hesine.hichat.model.request.ReceiptMsg;
import com.hesine.hichat.model.request.SendMsg;

/**
 * validate {@link RegGroup}, {@link HttpPushMessage}, {@link HttpQueryMessage}.
 * 
 * @author wanghua
 * 
 */
public class DispatcherFactory {
	public static  DispatchResult<?> dispatcher(FullHttpRequest chunk) {
		return dispatcher(chunk.content().toString(CharsetUtil.UTF_8));
	}
	
	
	public static DispatchResult<?> dispatcher(String content){
		DispatchResult<?> result = null;
		Base request = JSON.parseObject(content, Base.class);
		ActionInfo ai = request.getActionInfo();
		if (ai != null) {
			return selectNextHandler(ai.getActionId(), content);
		}
		return result;
	}

	public static DispatchResult<?> selectNextHandler (int actionId, String request) {
		DispatchResult<?> result = null;
		switch (actionId) {
		case ActionInfo.ACTION_ID_SEND_MSG:
			result = buildResult(request, SendMsg.class, new SendMsgHandler() );
			break;
		case ActionInfo.ACTION_ID_RECV_MSG:
		    result = buildResult(request, Base.class, new RecvMsgHandler() );
		    break;	
		case ActionInfo.ACTION_ID_RECEIPT_MSG:
            result = buildResult(request, ReceiptMsg.class, new ReceiptMsgHandler() );
            break;
		case ActionInfo.ACTION_ID_JOIN_CHAT:
            result = buildResult(request, Base.class, new JoinChatHandler() );
            break;
		case ActionInfo.ACTION_ID_LOGIN_CHAT:
			result = buildResult(request, Base.class, new RegistWebSocketHandler() );
			break;
		case ActionInfo.ACTION_ID_LOGOUT_CHAT:
			result = buildResult(request, Base.class, new LogoutWebSocketHandler() );
			break;		
		case ActionInfo.ACTION_ID_USER_INFO:
		    result = buildResult(request, GetUserInfo.class, new GetAccountInfoHandler() );
		    break;
		case ActionInfo.ACTION_ID_GET_HIS_MSG:
		    result = buildResult(request, GetHisMsg.class, new GetHisMsgHandler() );
		    break;
		case ActionInfo.ACTION_ID_CHAT_LIST:
		    result = buildResult(request, ChatList.class, new GetChatListHandler() );
		    break;
		case ActionInfo.ACTION_ID_GET_MEMBER_LIST:
		    result = buildResult(request, Base.class, new GetCustomerListHandler());
		    break;
		case ActionInfo.ACTION_ID_KICK_USER:
		    result = buildResult(request, ChatOperationRequest.class, new KickChatHandler() );
		    break;
		case ActionInfo.ACTION_ID_CHANGE_USER:
		    result = buildResult(request, ChatOperationRequest.class, new TransferChatHandler() );
		    break;
		case ActionInfo.ACTION_ID_CLOSE_CHAT:
		    result = buildResult(request, Base.class, new CloseChatHandler() );
		    break;
		case ActionInfo.ACTION_ID_NOTIFY:
            result = buildResult(request, NotifyRequest.class, new NotifyHandler() );
            break;
        case ActionInfo.ACTION_ID_CREATE_CHAT:
            result = buildResult(request, ChatOperationRequest.class, new CreateChatHandler() );
            break;
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param content
	 * @return
	 */
	public static <T> DispatchResult<T> buildResult(String request, Class<T> classType, SimpleChannelInboundHandler<T> inboundHandler) {
		T ar = JSON.parseObject(request,classType);
		DispatchResult<T> result = new DispatchResult<T>();
		result.setMessage(ar);
		result.setNextHandler(inboundHandler);
		return result;
	}
	
}
