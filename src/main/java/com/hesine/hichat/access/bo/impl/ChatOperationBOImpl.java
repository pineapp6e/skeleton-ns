package com.hesine.hichat.access.bo.impl;

import static com.hesine.hichat.access.common.EnumConstants.AUTH_ACCOUNT_USERTYPE_DOCTOR;
import static com.hesine.hichat.access.common.EnumConstants.CHAT_STATUS_CLOSE;
import static com.hesine.hichat.access.common.EnumConstants.HICHAT_ERROR_PARAM;
import static com.hesine.util.PropertiesUtil.getValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.bo.ChatBO;
import com.hesine.hichat.access.bo.ChatOperationBO;
import com.hesine.hichat.access.common.EnumConstants;
import com.hesine.hichat.access.dao.ChatOperationBAO;
import com.hesine.hichat.access.dao.UserDAO;
import com.hesine.hichat.access.model.ChatOperationRequest;
import com.hesine.hichat.access.model.ChatStatus;
import com.hesine.hichat.access.model.CustomerServiceStatus;
import com.hesine.hichat.access.service.ClientChannelCache;
import com.hesine.hichat.model.AccountInfo;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.ChatInfo;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.hichat.model.request.SendMsg;
import com.hesine.hichat.model.response.CustomerInfo;
import com.hesine.util.MessageUtil;

@Component("chatOperationBO")
public class ChatOperationBOImpl implements ChatOperationBO {

	private static Logger logger = Logger.getLogger(ChatOperationBOImpl.class.getName());
    @Autowired
    private ChatOperationBAO chatOperationBAO;
    @Autowired
    private AutoReplyBO autoReplyBO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private ChatBO chatBO;

    @Override
    public ChatStatus createChat(SendMsg msg){
		//Create a session,The people who take part in the session stored in tb_chat_user_r table 
		Long chatId = MessageUtil.generateChatId();
		logger.info("generate chatId is "+chatId);
		msg.getMessageInfo().setChatId(chatId);
		
		String virtualNumber = null;		
		/**
		 *   虚拟号字段，客服和用户之间聊天都存储虚拟号.
		 *   客服与客服及用户和用户之间聊天，虚拟号都存和FROM相反的,即第一条chme_user_account存From时,
		 *   虚拟号就存To，第二条chme_user_account存To时，虚拟号就存From
		 */
		//处理接收者
		int status;
		if(msg.getMessageInfo().getType()!=MessageInfo.TYPE_COMMON_USER_AND_CUSTOMER){
			chatOperationBAO.createChat(msg,chatId,msg.getMessageInfo().getTo(),msg.getActionInfo().getUserId(),EnumConstants.CHAT_STATUS_OPEN);
		}
		//处理发送者
		if(msg.getMessageInfo().getType()==MessageInfo.TYPE_COMMON_USER_AND_CUSTOMER){			
			virtualNumber = getValue("service.account");
			status = EnumConstants.CHAT_STATUS_DISPATCH;
		}else{
			virtualNumber = msg.getMessageInfo().getTo(); 
			status = EnumConstants.CHAT_STATUS_OPEN;
		}
		chatOperationBAO.createChat(msg,chatId,msg.getActionInfo().getUserId(),virtualNumber,status);
		
		//处理是否需要自动回复消息到客户端
		if(msg.getMessageInfo().getType() == MessageInfo.TYPE_COMMON_USER_AND_CUSTOMER){
			autoReplyBO.autoReply(AutoReplyBO.TYPE_CREATE_CHAT,msg);
		}
		
		//添加新增会话人
		ClientChannelCache.addChatPerson(msg.getActionInfo().getAppKey(), msg.getActionInfo().getUserId());
		return ChatStatus.build(chatId, status);
	}   
    @Override
    public AccountInfo getAccountInfo(String appKey,String userAccount){
    	return userDAO.getAccountInfo(appKey, userAccount);
    }
    
    @Override
    public int joinChat(ChatOperationRequest cor) {
        if (cor.getChatUserInfo() == null || cor.getChatUserInfo().getDoctorId() == null
                || cor.getChatUserInfo().getDoctorId().isEmpty()) {
            return HICHAT_ERROR_PARAM;
        }
        if (chatOperationBAO.checkChatUser(cor.getChatUserInfo().getChatId(), cor.getChatUserInfo()
                .getDoctorId()) < 1) {
            chatOperationBAO.insertChatUser(cor.getChatUserInfo().getChatId(), cor
                    .getChatUserInfo().getDoctorId());
        }
        insertDoctorAccount(cor.getChatUserInfo().getDoctorId(), cor.getChatUserInfo().getHospitalId());
        return 0;
    }
    
    @Override
    public List<CustomerInfo> getCustomerList(Base msg){
    	List<CustomerInfo> customerList = chatOperationBAO.getCustomerList(msg);
        return customerList;
    }
    
    public ChatInfo getChatStatusInfo(String userId,String serviceNumber){
    	ChatInfo chatStatusInfo = chatOperationBAO.getChatStatusInfo(userId, serviceNumber);
    	return chatStatusInfo;
    }

    @Override
    public int kickChat(ChatOperationRequest cor) {
        if (cor.getChatUserInfo() == null || cor.getChatUserInfo().getDoctorId() == null
                || cor.getChatUserInfo().getDoctorId().isEmpty()) {
            return HICHAT_ERROR_PARAM;
        }
        chatOperationBAO.deleteChatUser(cor.getChatUserInfo().getChatId(), cor.getChatUserInfo()
                .getDoctorId());
        return 0;
    }

    @Override
    public int transferChat(ChatOperationRequest cor) {
//        if (cor.getActionInfo().getDoctorId() == null
//                || cor.getActionInfo().getDoctorId().isEmpty()) {
//            return HICHAT_ERROR_USER;
//        }
//        if (cor.getChatUserInfo() == null
//                || cor.getChatUserInfo().getDoctorId() == null
//                || cor.getChatUserInfo().getDoctorId().isEmpty()) {
//            return HICHAT_ERROR_PARAM;
//        }
//        chatOperationBAO.updateChatUser(cor.getChatUserInfo().getChatId(), cor.getActionInfo()
//                .getDoctorId(), cor.getChatUserInfo().getDoctorId());
//        insertDoctorAccount(cor.getChatUserInfo().getDoctorId(), cor.getChatUserInfo().getHospitalId());
        return 0;
    }

    @Override
    public int closeChat(Base base) { 
    	CustomerServiceStatus css = ClientChannelCache.getCustomerServiceStatus(MessageUtil.generateServiceKey(base.getActionInfo().getAppKey(), base.getActionInfo().getUserId()));
    	if(css!=null){
    		ClientChannelCache.decreaseCss(base.getActionInfo().getAppKey(),css);
    		ClientChannelCache.clostChat(base.getActionInfo().getAppKey(), base.getActionInfo().getChatId(), base.getActionInfo().getUserId());
    	}
    	SendMsg autoReplymsg = new SendMsg();
		ActionInfo actionInfo = new ActionInfo();
		actionInfo.setAppKey(base.getActionInfo().getAppKey());		
		actionInfo.setUserId(base.getActionInfo().getUserId());		
		autoReplymsg.setActionInfo(actionInfo);		
		MessageInfo messageInfo = new MessageInfo();
		messageInfo.setChatId(base.getActionInfo().getChatId());
		autoReplymsg.setMessageInfo(messageInfo);
		//关闭会话如后台设置自动回复的话,需要读取设置内容回复给客户端
    	autoReplyBO.autoReply(AutoReplyBO.TYPE_CLOSE_CHAT,autoReplymsg);
        chatOperationBAO.updateChatStatus(base.getActionInfo().getChatId(), CHAT_STATUS_CLOSE);
        
        return 0;
    }
    
    private void insertDoctorAccount(String doctorId, String hospitalId) {
        if (doctorId != null && !doctorId.isEmpty()) {
            if (chatOperationBAO.checkAuthAccount(doctorId) < 1) {
                chatOperationBAO.insertAuthAccount(doctorId, AUTH_ACCOUNT_USERTYPE_DOCTOR);
            }

            if (hospitalId != null && !hospitalId.isEmpty()) {
                if (chatOperationBAO.checkGroup(hospitalId) < 1) {
                    chatOperationBAO.insertGroup(hospitalId);
                }
                if (chatOperationBAO.checkUserExtend(doctorId, hospitalId) < 1) {
                    chatOperationBAO.insertUserExtend(doctorId, hospitalId);
                }
            }
        }
    }

    @Override
	public int inchatCnt(String csAccount) {
		return chatOperationBAO.getInchatCnt(csAccount);
	}
	@Override
	public Map<String, Set<String>> dayChatPersonAppKey(boolean filterByPerson) {
		Map<String, Set<String>> result = chatOperationBAO.getChatPersonMapByAppkey(filterByPerson);
		return result;
	}
	@Override
	public String getCustomerAccount(Long chatId, String userId) {
		return chatOperationBAO.getOtherChatUser(chatId, userId);
	}

}
