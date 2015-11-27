/**
 * 
 */
package com.papple.framework.handler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;

import com.papple.framework.model.DispatchResult;

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
		//select next handler logic add here
		return result;
	}

}
