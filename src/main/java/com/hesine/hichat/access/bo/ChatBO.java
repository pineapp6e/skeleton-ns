/**
 * 
 */
package com.hesine.hichat.access.bo;

import java.util.List;

import com.hesine.hichat.model.AccountInfo;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.HisMsgParam;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.hichat.model.request.ReceiptMsg;
import com.hesine.hichat.model.request.SendMsg;

/**
 * @author wanghua
 *
 */
public interface ChatBO {

	/**
	 * 检查上线的账号
	 * @param actionInfo
	 * @return
	 */
	AccountInfo checkAccount(ActionInfo actionInfo);

	void clientSendMsg(SendMsg msg) throws Exception;

	/**
	 * 获取下发消息队列
	 * @param account
	 * @return
	 */
	boolean hasUnreadMsg(String account);

    List<MessageInfo> getClientNewMessages(String account, long chatId);
    List<MessageInfo> getClientNewMessages(String account, String deviceToken);

    List<MessageInfo> getHistoryMessages(ActionInfo actionInfo, HisMsgParam hisMsgParam);    
    
    /**
     * web客户端 、客服注销操作
     * @param msg
     */
	void logout(Base msg);

	 /**
     * web客户端 、客服登录操作
     * @param msg
     */
	void login(String userAccount,String appKey);
	
	int updateMsgQueueById(ReceiptMsg msg, String account);

}
