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
import com.hesine.hichat.access.bo.ChatOperationBO;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.util.DataAccessFactory;
import com.hesine.util.MessageUtil;

public class CloseChatHandler extends SimpleChannelInboundHandler<Base> {
    private static Logger logger = Logger.getLogger(CloseChatHandler.class);
    private ChatOperationBO chatOperationBO = (ChatOperationBO) DataAccessFactory.dataHolder().get(
            "chatOperationBO");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Base msg)
            throws Exception {
        logger.info("CloseChat Request : " + JSON.toJSONString(msg));

        int ret = chatOperationBO.closeChat(msg);

        com.hesine.hichat.model.response.Base response = MessageUtil.getSimpleResponse(ret, ActionInfo.ACTION_ID_CLOSE_CHAT);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        sendHttpResponse(ctx, res);
        logger.info("CloseChat Response : " + JSON.toJSONString(response));
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        /* ctx.channel().writeAndFlush("exception"); */
        com.hesine.hichat.model.response.Base response = MessageUtil.getSimpleResponse(HICHAT_ERROR_EXCEPTION_SERVER,
                ActionInfo.ACTION_ID_CLOSE_CHAT);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        logger.info("CloseChat Exception Response : " + JSON.toJSONString(response));
        ctx.channel().writeAndFlush(res);
        ctx.close();
    }

}
