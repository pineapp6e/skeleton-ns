/**
 * 
 */
package com.hesine.hichat.access.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.bo.ChatBO;
import com.hesine.hichat.access.service.ClientChannelCache;
import com.hesine.hichat.model.AccountInfo;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.util.DataAccessFactory;
import com.hesine.util.MessageUtil;

/**
 * 注册 websocket channel 到 map中，标记客户端身份， 并根据身份做相应业务处理
 * 
 * @author wanghua
 *
 */
public class RegistWebSocketHandler extends SimpleChannelInboundHandler<Base> {

	private static Logger logger = Logger
			.getLogger(RegistWebSocketHandler.class);

	private ChatBO chatBO = (ChatBO) DataAccessFactory.dataHolder().get(
			"chatBO");

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Base msg)
			throws Exception {
		logger.info("client connect server, client info : "
				+ JSON.toJSONString(msg));
		ActionInfo ai = msg.getActionInfo();
		AccountInfo account = null;
		if (null == ai.getUserId()) {
			com.hesine.hichat.model.response.Base response = MessageUtil
					.getSimpleResponse(1, ActionInfo.ACTION_ID_LOGIN_CHAT);
			logger.info("userId can't be empty:" + JSON.toJSONString(response));
			ctx.channel().writeAndFlush(
					new TextWebSocketFrame(JSON.toJSONString(response)));

			return;
		} else {
			account = chatBO.checkAccount(ai);
			if (null == account) {
				com.hesine.hichat.model.response.Base response = MessageUtil
						.getSimpleResponse(1, ActionInfo.ACTION_ID_LOGIN_CHAT);
				logger.info("user is not exist in database"
						+ JSON.toJSONString(response));
				ctx.channel().writeAndFlush(
						new TextWebSocketFrame(JSON.toJSONString(response)));
				return;
			}
		}
		registChannel(ctx, account);
		ctx.pipeline().remove(this);
	}

	private void registChannel(ChannelHandlerContext ctx, AccountInfo account) {
		if (account != null) {
			/**
			 * online,notify other customer
			 */
			com.hesine.hichat.model.response.Base onlineMsg = new com.hesine.hichat.model.response.Base();
			onlineMsg.setActionId(ActionInfo.ACTION_ID_USER_ON_LINE);
			onlineMsg.setUserAccount(account.getUserId());

			NotifyClientUtil.notifyGroup(JSON.toJSONString(onlineMsg),
					account.getAppKey(), null);
			Channel channel = ClientChannelCache.getClient(MessageUtil
					.generateServiceKey(account.getAppKey(),
							account.getUserId()));
			if (null != channel && channel == ctx.channel()) {
				return;
			}
			if(channel != null){
				channel.writeAndFlush(new CloseWebSocketFrame());
				channel.close();
				ClientChannelCache.removeCustomerService(account.getAppKey(),
						account.getUserId());
				logger.info("send closeFrame to channel " + account.getUserId());
			}
			logger.info("begin add " + account.getUserId());
			ClientChannelCache.add(account, ctx.channel());
			logger.info("end add " + account.getUserId());
			com.hesine.hichat.model.response.Base response = MessageUtil
					.getSimpleResponse(0, ActionInfo.ACTION_ID_LOGIN_CHAT);
			ctx.channel().writeAndFlush(
					new TextWebSocketFrame(JSON.toJSONString(response)));
			logger.info("login response:" + JSON.toJSONString(response));
			chatBO.login(account.getUserId(), account.getAppKey());
			ctx.channel().attr(WebSocketServerHandler.CLIENT_KEY)
					.set(account.getUserId());
			ctx.channel().attr(WebSocketServerHandler.APP_KEY)
					.set(account.getAppKey());

			/**
			 * check if has notify in queue.
			 */
			boolean hasUnreadMsg = chatBO.hasUnreadMsg(account.getUserId());
			if (hasUnreadMsg) {
				/**
				 * send notify to customer service, recieve msg.
				 */
				com.hesine.hichat.model.response.Base notifyMsg = MessageUtil
						.getSimpleResponse(0,
								ActionInfo.ACTION_ID_NEW_MSG_NOTICE);
				NotifyClientUtil.notifyBySocket(JSON.toJSONString(notifyMsg),
						MessageUtil.generateServiceKey(account.getAppKey(),
								account.getUserId()));
			}

			ClientChannelCache.notifyWait(account.getAppKey(),
					account.getUserId());
			
			ClientChannelCache.statisticData();
		} else {
			ctx.channel().writeAndFlush(new CloseWebSocketFrame());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error(cause.getMessage(), cause);
		ctx.channel().writeAndFlush(new CloseWebSocketFrame());
	}

}
