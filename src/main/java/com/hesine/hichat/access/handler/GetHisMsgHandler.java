package com.hesine.hichat.access.handler;

import static com.hesine.hichat.access.common.EnumConstants.HICHAT_ERROR_EXCEPTION_SERVER;
import static com.hesine.hichat.access.common.EnumConstants.HICHAT_ERROR_SERVER;
import static com.hesine.hichat.access.common.EnumConstants.HICHAT_ERROR_USER;
import static com.hesine.hichat.access.handler.ResponseGenerator.sendHttpResponse;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.bo.ChatBO;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.request.GetHisMsg;
import com.hesine.hichat.model.response.Base;
import com.hesine.hichat.model.response.RecvMsg;
import com.hesine.util.AccountUtil;
import com.hesine.util.DataAccessFactory;
import com.hesine.util.JSONTool;
import com.hesine.util.MessageUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

public class GetHisMsgHandler extends SimpleChannelInboundHandler<GetHisMsg> {
    private static Logger logger = Logger.getLogger(GetHisMsgHandler.class);
    private ChatBO chatBO = (ChatBO) DataAccessFactory.dataHolder().get("chatBO");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GetHisMsg msg) throws Exception {
        logger.info("GetHisMsg Request : " + JSON.toJSONString(msg));
        List<MessageInfo> messages = null;
        String account = AccountUtil.getRequestAccount(msg);

        int check = checkHisMsgRequest(account, msg);
        if (check == 0) {
            messages = chatBO.getHistoryMessages(msg.getActionInfo(), msg.getHisMsgParam());
        }

        RecvMsg response = getResponse(check, account, messages);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        sendHttpResponse(ctx, res);
        logger.info("GetHisMsg Response : " + JSONTool.getJSONStringExceptField(response,"attachment"));        
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        Base response = MessageUtil.getSimpleResponse(HICHAT_ERROR_EXCEPTION_SERVER,
                ActionInfo.ACTION_ID_GET_HIS_MSG);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        logger.info("Notify Exception Response : " + JSON.toJSONString(response));
        ctx.channel().writeAndFlush(res);
        ctx.close();
    }

    private RecvMsg getResponse(int check, String account, List<MessageInfo> messages) {
        RecvMsg response = new RecvMsg();
        response.setActionId(ActionInfo.ACTION_ID_GET_HIS_MSG);
        if (check != 0) {
            response.setCode(check);
            response.setMessage("failure");
            return response;
        }
        if (messages == null) {
            response.setCode(HICHAT_ERROR_SERVER);
            response.setMessage("failure");
            return response;
        }
        response.setUserAccount(account);
        response.setCode(0);
        response.setMessage("success");
        response.setMessages(messages);

        return response;
    }

    private int checkHisMsgRequest(String account, GetHisMsg request) {
        if (account == null || account.isEmpty()) {
            return HICHAT_ERROR_USER;
        }
        /*HisMsgParam hisMsgParam = request.getHisMsgParam();
        if (hisMsgParam == null || hisMsgParam.getOffset() < 0 || hisMsgParam.getLimit() < 0) {
            return HICHAT_ERROR_PARAM;
        }*/

        return 0;
    }

}
