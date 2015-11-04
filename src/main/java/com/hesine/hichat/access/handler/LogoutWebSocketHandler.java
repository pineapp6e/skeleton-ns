/**
 * 
 */
package com.hesine.hichat.access.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.bo.ChatBO;
import com.hesine.hichat.model.request.Base;
import com.hesine.util.DataAccessFactory;

/**
 * 客户端注销websocket长连接,日志记录注销动作，更新DB状态为下线，通知其它医生下线（如果是医生下线的话）
 * 
 * @author wanghua
 *
 */
public class LogoutWebSocketHandler extends SimpleChannelInboundHandler<Base> {

	private static Logger logger = Logger.getLogger(LogoutWebSocketHandler.class);

	private ChatBO chatBO = (ChatBO) DataAccessFactory.dataHolder().get(
			"chatBO");

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Base msg)
			throws Exception {
		logger.info("client logout server, client info : "
				+ JSON.toJSONString(msg));
		ctx.channel().writeAndFlush(new CloseWebSocketFrame());
		String appKey = ctx.channel().attr(WebSocketServerHandler.APP_KEY).get();
		msg.getActionInfo().setAppKey(appKey);
		chatBO.logout(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error(cause.getMessage(), cause);
		ctx.channel().writeAndFlush(new CloseWebSocketFrame());
	}

}
