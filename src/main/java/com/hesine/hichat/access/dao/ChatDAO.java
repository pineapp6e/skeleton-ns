/**
 * 
 */
package com.hesine.hichat.access.dao;

import java.util.List;

import com.hesine.hichat.access.model.AuthAccount;
import com.hesine.hichat.access.model.PatientClientStatus;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.DispatchChatInfo;
import com.hesine.hichat.model.HisMsgParam;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.request.SendMsg;

/**
 * @author wanghua
 *
 */
public interface ChatDAO {
	
	int saveMsg(SendMsg msg);

	List<AuthAccount> getTargetUser(long chatId, String sendUserId, String deviceToken);

	void addMsgQueue(List<AuthAccount> userGroup,MessageInfo messageInfo);
	void addAttachment(MessageInfo messageInfo,ActionInfo actionInfo );
    List<MessageInfo> getNewMessages(String account, long chatId);
    List<MessageInfo> getNewMessages(String account, String deviceToken); 
    int checkMsgHisQueue(String account);
    int msgInsertHisQueue(String account);
    int deleteMsgQueue(String account);
	void updateOnline(String account, byte userStateOnline,String appKey, String deviceToken);
	List<MessageInfo> getHistoryMessages(String account, HisMsgParam hisMsgParam);
	PatientClientStatus checkOldPatient(String userId);
	void updateMsgQueueState(List<String> queueMsgIdList, String account, int status);
	void msgInsertHisQueue(List<String> msgIdList, String account, String deviceToken);
	void deleteMsgQueue(List<String> msgIdList, String account, String deviceToken);
    List<MessageInfo> getAllHistoryMessages(String account, HisMsgParam hisMsgParam);    
    public Long checkChatStatus(Long chatId,String exceptUserId);
    void updateMsgReadStatus(List<String> recvMsgList, byte msgReaded);

    /**
     * 只适用于自动分配到客服的待待接入会话，生成队列
     * @param oldestDic  最老的待接入会话
     * @param account 接入的客服
     */
	void addMsgQueue(DispatchChatInfo oldestDic, String userId, String appKey);
}
