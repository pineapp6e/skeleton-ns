package com.hesine.hichat.access.handler;

import static com.hesine.hichat.access.handler.ResponseGenerator.sendHttpResponse;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.biz.exception.ErrorProcessException;
import com.hesine.hichat.access.bo.ChatBO;
import com.hesine.hichat.model.AccountInfo;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.request.SendMsg;
import com.hesine.hichat.model.response.Base;
import com.hesine.util.DataAccessFactory;
import com.hesine.util.JSONTool;
import com.hesine.util.MessageUtil;

public class SendMsgHandler extends SimpleChannelInboundHandler<SendMsg> {

	private static Logger logger = Logger.getLogger(SendMsgHandler.class);

	private ChatBO chatBO = (ChatBO) DataAccessFactory.dataHolder().get(
			"chatBO");
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SendMsg msg)
			throws Exception {
		logger.info("request: excpete attahcInfo " + JSONTool.getJSONStringExceptField(msg, "attachment"));
		MessageInfo messageInfo = msg.getMessageInfo();
		AccountInfo account = null;
		if(null == msg.getActionInfo().getUserId() || null == msg.getActionInfo().getAppKey()){			
			MessageUtil.errorResponse(ctx,ActionInfo.ACTION_ID_SEND_MSG, "userId or appkey can't be empty");		
			return;
		}else{
			account = chatBO.checkAccount(msg.getActionInfo());
			if(null == account){
				MessageUtil.errorResponse(ctx,ActionInfo.ACTION_ID_SEND_MSG, "user is not exist in database");					
				return;
			}
		}
		//( !messageInfo.isAttachmentMark() && StringUtils.isEmpty(messageInfo.getBody())&&StringUtils.isEmpty(messageInfo.getSubject()))
		if (messageInfo == null) {
			MessageUtil.errorResponse(ctx,ActionInfo.ACTION_ID_SEND_MSG, "invalid message.");					
			return;
		}
		if (messageInfo.getType() > MessageInfo.TYPE_COMMON_USER_AND_COMMON_USER 
				|| messageInfo.getType()<MessageInfo.TYPE_COMMON_USER_AND_CUSTOMER) {
			MessageUtil.errorResponse(ctx,ActionInfo.ACTION_ID_SEND_MSG, 
					"message type is error,The message type is between 0 to 2");					
			return;
		}		
		
		
		/**
		 *  save msg to DB, notice receiver
		 */
		try{
			chatBO.clientSendMsg(msg);
		}catch(ErrorProcessException e){
			logger.info(e.getMessage());			
			MessageUtil.errorResponse(ctx,ActionInfo.ACTION_ID_SEND_MSG, 
					"There is no customer in system");					
			return;
				
		}catch(Exception e){
			logger.info(e.getMessage());
		}
		
		/**
		 * response client
		 */
		Base response = new Base();
		response.setActionId(ActionInfo.ACTION_ID_SEND_MSG);
		response.setCode(0);
		response.setChatId(msg.getMessageInfo().getChatId());
		response.setUserAccount(msg.getActionInfo().getUserId());
		//if(msg.getActionInfo().getUserSource() == ActionInfo.ACTION_USRER_SRC_MOBILE){
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
				Unpooled.wrappedBuffer(JSON.toJSONBytes(response)));
		sendHttpResponse(ctx, res);
		logger.info("sendMsg Response : " + JSON.toJSONString(response));
		//}else{
			//ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON
				//	.toJSONString(response)));
		//}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error(cause.getMessage(), cause);
		Base response = new Base();
		response.setActionId(ActionInfo.ACTION_ID_SEND_MSG);
		response.setCode(1);
		response.setMessage(cause.getMessage());
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
				Unpooled.wrappedBuffer(JSON.toJSONBytes(response)));
		sendHttpResponse(ctx, res);
		logger.info("sendMsg Response : " + JSON.toJSONString(response));
	}

}
