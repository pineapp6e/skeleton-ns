package com.hesine.hichat.access.handler;

import static com.hesine.hichat.access.common.EnumConstants.HICHAT_ERROR_EXCEPTION_SERVER;
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
import com.hesine.hichat.access.bo.ChatStatusBO;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.ChatList;
import com.hesine.hichat.model.response.Base;
import com.hesine.util.DataAccessFactory;
import com.hesine.util.MessageUtil;

public class GetChatListHandler extends SimpleChannelInboundHandler<ChatList> {
    private static Logger logger = Logger.getLogger(GetChatListHandler.class);
    private ChatStatusBO chatStatusBO = (ChatStatusBO) DataAccessFactory.dataHolder().get(
            "chatStatusBO");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatList msg) throws Exception {
    	logger.info("ClientRecvMsg Request : " + JSON.toJSONString(msg));
        com.hesine.hichat.model.response.ChatList response = chatStatusBO.getChatList(msg);
        response.setActionId(ActionInfo.ACTION_ID_CHAT_LIST);
        if (response.getCode() == 0) {
            response.setMessage("success");
        } else {
            response.setMessage("failure");
        }
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        sendHttpResponse(ctx, res);
        logger.info("GetChatList Response : " + JSON.toJSONString(response));
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        Base response = MessageUtil.getSimpleResponse(HICHAT_ERROR_EXCEPTION_SERVER,
                ActionInfo.ACTION_ID_CHAT_LIST);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        logger.info("GetChatList Exception Response : " + JSON.toJSONString(response));
        ctx.channel().writeAndFlush(res);
        ctx.close();
    }

}
