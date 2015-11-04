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
import com.hesine.hichat.access.bo.NotifyBO;
import com.hesine.hichat.access.model.NotifyRequest;
import com.hesine.hichat.access.model.NotifyResponse;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.response.Base;
import com.hesine.util.AccountUtil;
import com.hesine.util.DataAccessFactory;
import com.hesine.util.MessageUtil;

public class NotifyHandler extends SimpleChannelInboundHandler<NotifyRequest> {
    private static Logger logger = Logger.getLogger(NotifyHandler.class);
    private NotifyBO notifyBO = (NotifyBO) DataAccessFactory.dataHolder().get("notifyBO");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NotifyRequest msg) throws Exception {
        logger.info("Notify Request : " + JSON.toJSONString(msg));
        String account = AccountUtil.getRequestAccount(msg);

        int ret = notifyBO.notifyProcess(account, msg);

        NotifyResponse response = getResponse(ret, account, msg);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        sendHttpResponse(ctx, res);
        logger.info("Notify Response : " + JSON.toJSONString(response));
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        Base response = MessageUtil.getSimpleResponse(HICHAT_ERROR_EXCEPTION_SERVER,
                ActionInfo.ACTION_ID_CLOSE_CHAT);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        logger.info("Notify Exception Response : " + JSON.toJSONString(response));
        ctx.channel().writeAndFlush(res);
        ctx.close();
    }

    private NotifyResponse getResponse(int ret, String account, NotifyRequest msg) {
        NotifyResponse response = new NotifyResponse();
        response.setActionId(msg.getActionInfo().getActionId());
        response.setUserAccount(account);
        response.setCode(ret);
        if (ret == 0) {
            response.setMessage("success");
        } else {
            response.setMessage("failure");
        }
        return response;
    }

}
