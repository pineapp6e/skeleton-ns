package com.hesine.hichat.access.handler;

import static com.hesine.hichat.access.common.EnumConstants.HICHAT_ERROR_EXCEPTION_SERVER;
import static com.hesine.hichat.access.handler.ResponseGenerator.sendHttpResponse;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.bo.ChatOperationBO;
import com.hesine.hichat.access.model.ChatOperationRequest;
import com.hesine.hichat.access.model.ChatOperationResponse;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.response.Base;
import com.hesine.util.DataAccessFactory;
import com.hesine.util.MessageUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

public class KickChatHandler extends SimpleChannelInboundHandler<ChatOperationRequest> {
    private static Logger logger = Logger.getLogger(KickChatHandler.class);
    private ChatOperationBO chatOperationBO = (ChatOperationBO) DataAccessFactory.dataHolder().get("chatOperationBO");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatOperationRequest msg)
            throws Exception {
        logger.info("KickChat Request : " + JSON.toJSONString(msg));

        int ret = chatOperationBO.kickChat(msg);

        ChatOperationResponse response = MessageUtil.getChatOperationResponse(ret, msg);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        sendHttpResponse(ctx, res);
        logger.info("KickChat Response : " + JSON.toJSONString(response));
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        Base response = MessageUtil.getSimpleResponse(HICHAT_ERROR_EXCEPTION_SERVER,
                ActionInfo.ACTION_ID_CLOSE_CHAT);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        logger.info("KickChat Exception Response : " + JSON.toJSONString(response));
        ctx.channel().writeAndFlush(res);
        ctx.close();
    }

}
