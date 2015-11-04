package com.hesine.hichat.access.bo.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.common.EnumConstants;
import com.hesine.hichat.access.dao.ChatOperationBAO;
import com.hesine.hichat.access.model.SettingInfo;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.request.SendMsg;
import com.hesine.util.MessageUtil;

@Component("autoReplyBO")
public class AutoReplyBO {
	private static Logger logger = Logger.getLogger(AutoReplyBO.class);
	
	public static final int TYPE_CREATE_CHAT = 1;
	public static final int TYPE_CLOSE_CHAT = 2;
	
	private int autoReplyType;
	private Map<Integer,SettingInfo> settingInfoMap;
	
	@Autowired
	private ChatOperationBAO chatOperationDAO;
	@Autowired
    private PdChatBO chatBO;
	
	public void autoReply(int type, SendMsg msg){
		
		settingInfoMap = chatOperationDAO.getSettingInfo(msg.getActionInfo().getAppKey());
		boolean flag = isNeedAutoReply(type);
		if(!flag){
			logger.info("no need auto reply");
			return;
		}	
		SendMsg autoReplymsg = new SendMsg();
		ActionInfo actionInfo = new ActionInfo();
		actionInfo.setAppKey(msg.getActionInfo().getAppKey());
		actionInfo.setUserType(ActionInfo.ACTION_USRER_TYPE_CUSTOMER);
		if(AutoReplyBO.TYPE_CREATE_CHAT==type){
			actionInfo.setUserId(msg.getMessageInfo().getTo());
		}else{
			actionInfo.setUserId(msg.getActionInfo().getUserId());
		}
		autoReplymsg.setActionInfo(actionInfo);
		
		MessageInfo messageInfo = new MessageInfo();
		if(AutoReplyBO.TYPE_CREATE_CHAT==type){
			messageInfo.setFrom(msg.getMessageInfo().getTo());
			messageInfo.setTo(msg.getMessageInfo().getFrom());
		}else{
			messageInfo.setFrom(msg.getActionInfo().getUserId());			
		}
		messageInfo.setChatId(msg.getMessageInfo().getChatId());
		messageInfo.setType(MessageInfo.TYPE_COMMON_USER_AND_CUSTOMER);
		messageInfo.setSubType(MessageInfo.SUB_TYPE_CUSTOMER_AUTO_REPLY);
		messageInfo.setMsgId(MessageUtil.getMessageId(actionInfo));
		messageInfo.setSource(ActionInfo.ACTION_USRER_TYPE_CUSTOMER);
		messageInfo.setBody(settingInfoMap.get(autoReplyType).getContent());
		autoReplymsg.setMessageInfo(messageInfo);
		
		chatBO.saveMsgAndNotify(autoReplymsg,EnumConstants.CHAT_STATUS_OPEN, 0);
	}
	
	private boolean isNeedAutoReply(int type){			
		switch (type){
			case AutoReplyBO.TYPE_CREATE_CHAT:
				//检查是否有自动回复设置
				if(!settingInfoMap.containsKey(SettingInfo.TYPE_GETOFF_WORK_REMINDER)){
					return false;
				}
				//检查是否设置下班自动回复
				SettingInfo getOffWorkReminder = settingInfoMap.get(SettingInfo.TYPE_GETOFF_WORK_REMINDER);
				if(getOffWorkReminder.getIsSelected()==SettingInfo.NOT_SELECTED){
					//检查是否有自定义回复设置
					return isHaveCustomSetting();
				}else{								
					
					Calendar cal = Calendar.getInstance();
		            cal.setTime(new Date());
		            //当前是星期几
		            int weekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;		             
					SettingInfo workDay = settingInfoMap.get(SettingInfo.TYPE_WORK_DAY_SETTING);
					char isSelected = workDay.getContent().charAt(weekDay);
					
					char NotSelected = '0';
					//星期没有设置
					if(isSelected==NotSelected){						
						//检查是否有自定义回复设置
						return isHaveCustomSetting();
					}else{					
						boolean isWorkTime = isWorkTime();
						if(isWorkTime){
							//工作时间以内
							return isHaveCustomSetting();
						}else{
							//工作时间外
				    		autoReplyType = SettingInfo.TYPE_GETOFF_WORK_REMINDER;
				    		return true;
						}	
					}
				}
				//关闭会话				
			case AutoReplyBO.TYPE_CLOSE_CHAT:
				return isCloseChatSetting();
			default:
				return false;
		}				
	}
	
	//判断是否有自定义设置
	private boolean isHaveCustomSetting(){
		SettingInfo customerSetting = settingInfoMap.get(SettingInfo.TYPE_CUSTOM_SETTING);
		if(customerSetting.getIsSelected()==SettingInfo.NOT_SELECTED){
			return false;
		}else{
			autoReplyType = SettingInfo.TYPE_CUSTOM_SETTING;
			return true;
		}
	}
	
	//判断是否在工作时间
	private boolean isWorkTime(){	
		SettingInfo workTime = settingInfoMap.get(SettingInfo.TYPE_WORK_TIME_SETTING);								
		String[] time = workTime.getContent().split("-");
		String[] startHourAndMinutes = time[0].split(":");
		String[] endHourAndMinutes = time[1].split(":");
		Calendar c = Calendar.getInstance();
    	int currHour = c.get(Calendar.HOUR_OF_DAY); 
    	int currMinute = c.get(Calendar.MINUTE);
    	//工作时间内
    	if(currHour>Integer.parseInt(startHourAndMinutes[0])    			
    			&& currHour < Integer.parseInt(endHourAndMinutes[0])){
    		return true;
    		
    	}else if(currHour==Integer.parseInt(startHourAndMinutes[0]) 
    			&& currMinute>=Integer.parseInt(startHourAndMinutes[1])){
    		return true;
    	}else if(currHour==Integer.parseInt(endHourAndMinutes[0]) 
    			&& currMinute<=Integer.parseInt(endHourAndMinutes[1])){
    		return true;
    	}else{
    		return false;
    	}
	}
	private boolean isCloseChatSetting(){		
		//判断是否设置关闭提示
		SettingInfo closeChatSetting = settingInfoMap.get(SettingInfo.TYPE_CLOSE_CHAT_SETTING);
		if(closeChatSetting.getIsSelected()==SettingInfo.NOT_SELECTED){
			return false;
		}else{
			autoReplyType = SettingInfo.TYPE_CLOSE_CHAT_SETTING;
			return true;
		}
	}
}
