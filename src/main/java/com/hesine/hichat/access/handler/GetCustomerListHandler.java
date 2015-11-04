package com.hesine.hichat.access.handler;

import static com.hesine.hichat.access.common.EnumConstants.HICHAT_ERROR_EXCEPTION_SERVER;
import static com.hesine.hichat.access.handler.ResponseGenerator.sendHttpResponse;
import static com.hesine.util.PropertiesUtil.getValue;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.bo.ChatOperationBO;
import com.hesine.hichat.model.AccountInfo;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.ChatInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.hichat.model.response.CustomerInfo;
import com.hesine.hichat.model.response.CustomerList;
//import com.hesine.hichat.model.response.Base;
import com.hesine.util.DataAccessFactory;
import com.hesine.util.MessageUtil;

public class GetCustomerListHandler extends SimpleChannelInboundHandler<Base> {
    private static Logger logger = Logger.getLogger(GetCustomerListHandler.class);
    private ChatOperationBO chatOperationBO = (ChatOperationBO) DataAccessFactory.dataHolder().get(
            "chatOperationBO");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Base msg)
            throws Exception {
        logger.info("getMemberList Request : " + JSON.toJSONString(msg));
        ActionInfo actionInfo = msg.getActionInfo();
        if(actionInfo.getUserType()==ActionInfo.ACTION_USRER_TYPE_COMMON_USER){
        	CustomerList response = new CustomerList();
        	CustomerInfo customInfo = new CustomerInfo();
        	
        	String serviceAccount = getValue("service.account");
        	ChatInfo chatStatusInfo = chatOperationBO.getChatStatusInfo(actionInfo.getUserId(),serviceAccount);
        	customInfo.setLastChatInfo(chatStatusInfo);
        	
        	AccountInfo account = new AccountInfo();
            String serviceNickName = getValue("service.nickname");
        	account.setUserId(serviceAccount);
        	account.setNickName(serviceNickName);
        	customInfo.setAccountInfo(account);
        	
        	List<CustomerInfo> customerInfoList = new ArrayList<CustomerInfo>();
        	customerInfoList.add(customInfo);
        	
        	response.setCustomerInfoList(customerInfoList);
        	response.setActionId(ActionInfo.ACTION_ID_GET_MEMBER_LIST);
        	response.setCode(0);
        	FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                    Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
            sendHttpResponse(ctx, res);
            logger.info("serverNumber Response : " + JSON.toJSONString(response));
        	
        }else{
        	CustomerList response = new CustomerList();
        	List<CustomerInfo> customerList = chatOperationBO.getCustomerList(msg);
        	response.setCustomerInfoList(customerList);
        	response.setActionId(ActionInfo.ACTION_ID_GET_MEMBER_LIST);
        	response.setCode(0);
        	FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                    Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
            sendHttpResponse(ctx, res);
            logger.info("customerList Response : " + JSON.toJSONString(response));
        }

        
        
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        com.hesine.hichat.model.response.Base response = MessageUtil.getSimpleResponse(HICHAT_ERROR_EXCEPTION_SERVER,
                ActionInfo.ACTION_ID_CLOSE_CHAT);
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(JSON.toJSONString(response).getBytes()));
        logger.info("JoinChat Exception Response : " + JSON.toJSONString(response));
        ctx.channel().writeAndFlush(res);
        ctx.close();
    }

}
