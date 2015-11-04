/**
 * 
 */
package com.hesine.hichat.access.bo.impl;

import static com.hesine.hichat.access.common.EnumConstants.HISTORY_MESSAGES_DEFAULT;
import static com.hesine.util.PropertiesUtil.getValue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.hesine.hichat.access.biz.exception.ErrorProcessException;
import com.hesine.hichat.access.bo.ChatBO;
import com.hesine.hichat.access.bo.ChatOperationBO;
import com.hesine.hichat.access.common.EnumConstants;
import com.hesine.hichat.access.dao.ChatDAO;
import com.hesine.hichat.access.dao.ChatOperationBAO;
import com.hesine.hichat.access.dao.NotifyDAO;
import com.hesine.hichat.access.dao.UserDAO;
import com.hesine.hichat.access.handler.NotifyClientUtil;
import com.hesine.hichat.access.model.AuthAccount;
import com.hesine.hichat.access.model.ChatStatus;
import com.hesine.hichat.access.service.ClientChannelCache;
import com.hesine.hichat.model.AccountInfo;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.AttachInfo;
import com.hesine.hichat.model.ChatInfo;
import com.hesine.hichat.model.DispatchChatInfo;
import com.hesine.hichat.model.HisMsgParam;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.hichat.model.request.ReceiptMsg;
import com.hesine.hichat.model.request.SendMsg;
import com.hesine.util.AccountUtil;
import com.hesine.util.AttachFileContext;
import com.hesine.util.ImageUtil;
import com.hesine.util.MessageUtil;

/**
 * @author wanghua
 *
 */
@Component("chatBO")
public class PdChatBO implements ChatBO {

	private static Logger logger = Logger.getLogger(PdChatBO.class.getName());

	@Autowired
	private ChatDAO chatDAO;

	@Autowired
	private ChatOperationBAO chatOperationDAO;
	
	@Autowired
	private NotifyDAO notifyDAO;
	@Autowired
    private ChatOperationBO chatOperationBO;
	@Autowired
    private UserDAO userDAO;
	
	@Override
	public void clientSendMsg(SendMsg msg) throws Exception {

		MessageInfo messageInfo = msg.getMessageInfo();
		ActionInfo actionInfo = msg.getActionInfo();		
		messageInfo.setMsgId(MessageUtil.getMessageId(actionInfo));
		
		ChatStatus chatStatus = null; 
		long chatId;
		int status = EnumConstants.CHAT_STATUS_OPEN;
		int isFirstChat = 0;
		if(null == messageInfo.getChatId()){
			chatStatus = chatOperationBO.createChat(msg);
			status = chatStatus.getStatus();
			chatId = chatStatus.getChatId();
			if(0 == chatId){
				throw new ErrorProcessException("chatId is null");
			}
		}else{
			isFirstChat = 1;
			chatId = messageInfo.getChatId();	
			//如果普通用户发给服务号的，直接用数据库的chatId
			if(messageInfo.getType()==MessageInfo.TYPE_COMMON_USER_AND_CUSTOMER 
					&& actionInfo.getUserType()==ActionInfo.ACTION_USRER_TYPE_COMMON_USER){
				ChatInfo chatStatusInfo = chatOperationBO.getChatStatusInfo(actionInfo.getUserId(),messageInfo.getTo());
	        	if(chatStatusInfo !=null && chatStatusInfo.getChatId()>0){
	        		chatId = chatStatusInfo.getChatId();
	        		status = chatStatusInfo.getChatStatus();
	        	}else{
	        		isFirstChat = 0;
	        		logger.info("chatId:"+chatId+" is already closed");
	        		chatStatus = chatOperationBO.createChat(msg);
	        		status = chatStatus.getStatus();
	        		chatId = chatStatus.getChatId();
	        		if(0 == chatId){
	    				throw new ErrorProcessException("chatId is null");
	    			}
	        	}
			}
		}
		messageInfo.setChatId(chatId);
		logger.info("current chatId is "+chatId);
		saveMsgAndNotify(msg, status, isFirstChat);
				
	}		
	
	public void saveMsgAndNotify(SendMsg msg, int chatStatus, int isFirstChat){
		
		MessageInfo messageInfo = msg.getMessageInfo();
		Long chatId = messageInfo.getChatId();
		ActionInfo actionInfo = msg.getActionInfo();
		String sendUserId = actionInfo.getUserId();
		if(msg.getActionInfo().getUserType()==ActionInfo.ACTION_USRER_TYPE_CUSTOMER && messageInfo.getType() == MessageInfo.TYPE_COMMON_USER_AND_CUSTOMER){
			messageInfo.setFrom(getValue("service.account"));
		}
		//save message.
		chatDAO.saveMsg(msg);
		ClientChannelCache.addMessageCnt(actionInfo.getAppKey());
		logger.info("save msgId is "+messageInfo.getMsgId());
		
		if (messageInfo.isAttachmentMark() && messageInfo.getAttachInfo() != null ) {
			persistAttach(messageInfo,actionInfo );
		}

		if(chatStatus == EnumConstants.CHAT_STATUS_OPEN){
			// save queue
			List<AuthAccount> talkGroup = chatDAO.getTargetUser(chatId, sendUserId, actionInfo.getDeviceToken());
			chatDAO.addMsgQueue(talkGroup,messageInfo);
			//notify acceptance.
			for(AuthAccount authAccount:talkGroup){
				if(authAccount.getConnectType() == EnumConstants.CONNECT_TYPE_FROM_MOBILE){
					NotifyClientUtil.notifyMobile(authAccount);
				}else{
					com.hesine.hichat.model.response.Base notifyMsg = MessageUtil.getSimpleResponse(0, ActionInfo.ACTION_ID_NEW_MSG_NOTICE);
					NotifyClientUtil.notifyBySocket(JSON.toJSONString(notifyMsg), MessageUtil.generateServiceKey(authAccount.getAppKey(),authAccount.getAccount()));
				}
			}	
		}else if(chatStatus == EnumConstants.CHAT_STATUS_DISPATCH && isFirstChat == 0){
			DispatchChatInfo dci = new DispatchChatInfo();
			dci.setChatId(chatId);
			dci.setCreateTime(msg.getMessageInfo().getTime());
			dci.setUserId(msg.getMessageInfo().getFrom());
			ClientChannelCache.addWaitChat(actionInfo.getAppKey(), dci);
			ClientChannelCache.addChatPerson(actionInfo.getAppKey(), sendUserId);
			ClientChannelCache.addChat(actionInfo.getAppKey(), String.valueOf(chatId));
		}
		
	}
	
	@Override
	public AccountInfo checkAccount(ActionInfo actionInfo) {
		AccountInfo accountInfo = null;		
		accountInfo = userDAO.getAccountInfo(actionInfo.getAppKey(), actionInfo.getUserId());		
		return accountInfo;
	}

	@Override
	public boolean hasUnreadMsg(String account){
		return notifyDAO.getQueueCnt(account)>0;
	}
	
	@Override
	public List<MessageInfo> getClientNewMessages(String account, String deviceToken) {
		if (account == null || account.isEmpty()) {
			return null;
		}
		
		List<MessageInfo> mis = chatDAO.getNewMessages(account, deviceToken);
		if (mis.size() == 0) {
			return mis;
		}
		List<String> queueMsgIdList = null;
		if (!CollectionUtils.isEmpty(mis)) {
			queueMsgIdList = new ArrayList<String>();
			for (MessageInfo messageInfo : mis) {
				queueMsgIdList.add(messageInfo.getMsgId());
				dealAttach(messageInfo);
			}
		}
		return mis;
	}
	private void dealAttach(MessageInfo messageInfo){
		AttachInfo attachInfo = messageInfo.getAttachInfo();
		if (attachInfo != null) {
			String attName = attachInfo.getName();
			String attUrl = attachInfo.getUrl();
			logger.info("att path:" + attUrl + ", attType:"
					+ attachInfo.getType());
			attUrl = attUrl.substring(0,
					attUrl.length() - attName.length());			
			String attachContent = null;
			if (attachInfo.getType() == AttachInfo.ATTACH_VIDEO) {
				attName = attName.substring(0, attName.indexOf("."))
						.concat(".jpg");				
			}
			if (attachInfo.getType() == AttachInfo.ATTACH_VIDEO &&
					attachInfo.getType() == AttachInfo.ATTACH_PIC){
				attachContent = AttachFileContext.getAttachContent(
						attUrl, attName, AttachFileContext.BASE_THUMBNAIL_PATH);
			}else{
				attachContent = AttachFileContext.getAttachContent(
						attUrl, attName, AttachFileContext.BASE_PATH);
			}
			attachInfo.setAttachment(attachContent);
			attachInfo.setUrl(AttachFileContext.ATTACH_URL_PREFIX+attachInfo.getUrl());
			messageInfo.setAttachInfo(attachInfo);
			
		}
	}
	private void dealAttachForWeb(MessageInfo messageInfo){
        AttachInfo attachInfo = messageInfo.getAttachInfo();
        if (attachInfo != null) {
            String attUrl = attachInfo.getUrl();
            logger.info("att path:" + attUrl + ", attType:"
                    + attachInfo.getType());
            attachInfo.setAttachment(null);
            attachInfo.setUrl(AttachFileContext.ATTACH_URL_PREFIX+attachInfo.getUrl());
            messageInfo.setAttachInfo(attachInfo);
            
        }
    }
	@Override
	public int updateMsgQueueById(ReceiptMsg msg, String account) {
		List<String> recvMsgList = msg.getRecvMsgList();
		if(CollectionUtils.isEmpty(recvMsgList) || StringUtils.isEmpty(account)){
			return 0;
		}
		String deviceToken = AccountUtil.getDeviceToken(msg.getActionInfo());
		chatDAO.msgInsertHisQueue(recvMsgList, account, deviceToken);
		chatDAO.deleteMsgQueue(recvMsgList, account, deviceToken);
		chatDAO.updateMsgReadStatus(recvMsgList, EnumConstants.MSG_READED);
		return recvMsgList.size();
	}

	@Override
	public List<MessageInfo> getClientNewMessages(String account, long chatId) {
		if (account == null || account.isEmpty()) {
			return null;
		}

		List<MessageInfo> mis = chatDAO.getNewMessages(account, chatId);
		if (mis.size() == 0) {
			return mis;
		}
		List<String> queueMsgIdList = null;
		if (!CollectionUtils.isEmpty(mis)) {
			queueMsgIdList = new ArrayList<String>();
			for (MessageInfo messageInfo : mis) {
				queueMsgIdList.add(messageInfo.getMsgId());
				dealAttach(messageInfo);
			}
		}
//		chatDAO.checkMsgHisQueue(account);
//		chatDAO.msgInsertHisQueue(account);
//		chatDAO.deleteMsgQueue(account);
		
		/**
		 * 状态设为-1，表示中间状态
		 */
		chatDAO.updateMsgQueueState(queueMsgIdList, account, 1);
		return mis;
	}

	private void persistAttach(MessageInfo messageInfo,ActionInfo actionInfo ) {
		/**
		 * 生成附件路径，如果是音频，放在项目目录下， 如果是其它附件，通过web服务获取，放到web目录下
		 */
		String relativeURL = AttachFileContext.genRelativePath(messageInfo
				.getFrom());
		StringBuilder attDec = new StringBuilder();

		if (messageInfo.getAttachInfo().getType() == AttachInfo.ATTACH_AUDIO) {
			attDec.append(AttachFileContext.BASE_THUMBNAIL_PATH);
		} else {
			attDec.append(AttachFileContext.BASE_PATH);
		}
		attDec.append(AttachFileContext.FILE_SEPARATOR);
		attDec.append(relativeURL);
		File attachPath = new File(attDec.toString());
		if (!attachPath.exists()) {
			attachPath.mkdirs();
		}		
		int suffix = (int)(Math.random()*1000);	
		String attachName = suffix+getFileSuffix(messageInfo.getAttachInfo().getName()); 
		messageInfo.getAttachInfo().setName(attachName);
		/**
		 * 保存附件到DB与文件中
		 */
		try {
			attDec.append(AttachFileContext.FILE_SEPARATOR);
			attDec.append(messageInfo.getAttachInfo().getName());
			logger.info("save attach path:"+attDec.toString());
			//attDec.append(attachName);
			if (StringUtils.isNotEmpty(messageInfo.getAttachInfo()
					.getAttachment())) {
				ImageUtil.saveFile(attDec.toString(), messageInfo
						.getAttachInfo().getAttachment());
			}

			messageInfo.getAttachInfo().setUrl(
					AttachFileContext.getURL(messageInfo.getAttachInfo().getName(), relativeURL));
			logger.info("set att url:" + messageInfo.getAttachInfo().getUrl());

//			if (messageInfo.getAttachInfo().getType() == AttachInfo.ATTACH_VIDEO) {
//				messageInfo.getAttachInfo().setName(
//						transPicName(messageInfo.getAttachInfo().getName()));
//			}
			
			chatDAO.addAttachment(messageInfo,actionInfo );

			/**
			 * 生成缩略图，只有图片跟视频的第一帧会生成
			 */
			StringBuilder target = new StringBuilder();
			target.append(AttachFileContext.BASE_THUMBNAIL_PATH);
			target.append(AttachFileContext.FILE_SEPARATOR);
			target.append(relativeURL);
			target.append(AttachFileContext.FILE_SEPARATOR);

			attachPath = new File(target.toString());
			if (!attachPath.exists()) {
				attachPath.mkdirs();
			}

			if (messageInfo.getAttachInfo().getType() == AttachInfo.ATTACH_PIC) {				
				target.append(messageInfo.getAttachInfo().getName());
				logger.info("save pic thumbnail path:"+target.toString());
				if (messageInfo.getAttachInfo().getSize() <= EnumConstants.COMPRESSION_STANDARD) {
					ImageUtil.saveFile(target.toString(), messageInfo
							.getAttachInfo().getAttachment());
				} else {
					ImageUtil.compress(attDec.toString(), target.toString());
				}
			} else if (messageInfo.getAttachInfo().getType() == AttachInfo.ATTACH_VIDEO) {					
				target.append(transPicName(messageInfo.getAttachInfo().getName()));
				logger.info(" save video thumbnail : " + target.toString());
				ImageUtil.screenshot(attDec.toString(), target.toString(), "1");
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private String transPicName(String videoName) {
		videoName = videoName.substring(0, videoName.indexOf("."));
		videoName += ".jpg";
		return videoName;
	}
	
	private String getFileSuffix(String fileName) {
		fileName = fileName.substring(fileName.indexOf("."),fileName.length());		
		return fileName;
	}

	@Override
	public List<MessageInfo> getHistoryMessages(ActionInfo actionInfo,
			HisMsgParam hisMsgParam) {
		if (StringUtils.isEmpty(actionInfo.getUserId())) {
			return null;
		}
		
		if (hisMsgParam == null) {
		    hisMsgParam = new HisMsgParam();		   
		    hisMsgParam.setLimit(HISTORY_MESSAGES_DEFAULT);		    		    
		}
		if (hisMsgParam.getLimit() <= 0) {
		    hisMsgParam.setLimit(HISTORY_MESSAGES_DEFAULT);
		}		
		
		List<MessageInfo> mis;		
		mis = chatDAO.getHistoryMessages(actionInfo.getUserId(), hisMsgParam);				
		if (!CollectionUtils.isEmpty(mis)) {
			for (MessageInfo messageInfo : mis) {
			    if (actionInfo.getUserSource() == ActionInfo.ACTION_USRER_SRC_WEBPAGE) {
			        dealAttachForWeb(messageInfo);
			    } else {
			        dealAttach(messageInfo);
			    }
			}
		}

		return mis;
	}
	@Override
	public void login(String userAccount,String appKey){		
		chatDAO.updateOnline(userAccount, EnumConstants.USER_STATE_ONLINE, appKey, userAccount);
	}

	@Override
	public void logout(Base msg) {
		ActionInfo ai = msg.getActionInfo();
		String account = null;
		if (StringUtils.isEmpty(ai.getUserId())) {
			logger.info("userId is null for offline");
		}else{
			/**
			 * offline,notify other customer
			 */
			account = ai.getUserId();			
			com.hesine.hichat.model.response.Base offlineMsg = new com.hesine.hichat.model.response.Base();
			offlineMsg.setActionId(ActionInfo.ACTION_ID_USER_OFF_LINE);
			offlineMsg.setUserAccount(account);
			NotifyClientUtil.notifyGroup(JSON.toJSONString(offlineMsg), ai.getAppKey(),null);
		}
				
	}

}
