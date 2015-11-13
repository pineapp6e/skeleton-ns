/**
 * 
 */
package com.hesine.hichat.access.handler;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.model.DispatchResult;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.Base;

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
			result = buildResult(request, Object.class, new HelloWorldHandler() );
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
