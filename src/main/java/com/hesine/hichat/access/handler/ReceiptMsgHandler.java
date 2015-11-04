package com.hesine.hichat.access.handler;

import static com.hesine.hichat.access.common.EnumConstants.HICHAT_ERROR_EXCEPTION_SERVER;
import static com.hesine.hichat.access.common.EnumConstants.HICHAT_ERROR_SERVER;
import static com.hesine.hichat.access.handler.ResponseGenerator.sendHttpResponse;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.bo.ChatBO;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.request.ReceiptMsg;
import com.hesine.hichat.model.response.Base;
import com.hesine.util.AccountUtil;
import com.hesine.util.DataAccessFactory;
import com.hesine.util.JSONTool;
import com.hesine.util.MessageUtil;

public class ReceiptMsgHandler extends SimpleChannelInboundHandler<ReceiptMsg> {
    private static Logger logger = Logger.getLogger(ReceiptMsgHandler.class);
    private ChatBO chatBO = (ChatBO) DataAccessFactory.dataHolder().get("chatBO");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReceiptMsg msg) throws Exception {
        logger.info("ClientReceipt Request : " + JSON.toJSONString(msg));
        String account = AccountUtil.getRequestAccount(msg);
        int updateCnt = chatBO.updateMsgQueueById(msg, account);
        int oldCnt = 0;
        if(CollectionUtils.isNotEmpty(msg.getRecvMsgList())){
        	oldCnt = msg.getRecvMsgList().size();
        }
        Base response = getResponse(account, updateCnt == oldCnt);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        sendHttpResponse(ctx, res);
        logger.info("ClientReceipt Response : " + JSONTool.getJSONStringExceptField(response,"attachment"));
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        /* ctx.channel().writeAndFlush("exception"); */
        com.hesine.hichat.model.response.Base response = MessageUtil.getSimpleResponse(
                HICHAT_ERROR_EXCEPTION_SERVER, ActionInfo.ACTION_ID_RECV_MSG);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        logger.info("ClientRecvMsg Exception Response : " + JSON.toJSONString(response));
        ctx.channel().writeAndFlush(res);
        ctx.close();
    }
    
    private Base getResponse(String account, boolean updateSuccess) {
        Base response = new Base();
        response.setActionId(ActionInfo.ACTION_ID_RECEIPT_MSG);
        if (!updateSuccess) {
            response.setCode(HICHAT_ERROR_SERVER);
            response.setMessage("failure");
            return response;
        }
        response.setUserAccount(account);
        response.setCode(0);
        response.setMessage("success");
        return response;
    }

}
