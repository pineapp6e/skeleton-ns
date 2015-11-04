package com.hesine.hichat.access.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.hesine.hichat.access.common.EnumConstants;
import com.hesine.hichat.access.dao.ChatDAO;
import com.hesine.hichat.access.model.AuthAccount;
import com.hesine.hichat.access.model.PatientClientStatus;
import com.hesine.hichat.model.ActionInfo;
import com.hesine.hichat.model.AttachInfo;
import com.hesine.hichat.model.DispatchChatInfo;
import com.hesine.hichat.model.HisMsgParam;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.request.SendMsg;

@Component("chatDAO")
public class PdChatDAO extends BaseDAO implements ChatDAO {
	private static final Logger logger = Logger.getLogger(PdChatDAO.class
			.getName());
    private static final String TABLE_AUTH_ACCOUNT = "hiauth.tb_auth_account_r";
    private static final String TABLE_MSG_BOX = "hichat.tb_msg_box_r";
    private static final String TABLE_CHAT_USER = "hichat.tb_chat_user_r";

    private static final String TB_MSG_BOX_COLUMN = "msbo_id,msbo_app_key,msbo_chat_id,msbo_from,"
            + "msbo_to,msbo_subject,msbo_content,"
            + "msbo_attach_mark,msbo_attach_path, msbo_is_read,msbo_source,"
            + "msbo_type,msbo_subtype,msbo_status,msbo_create_time ";

	private static final String TB_MSG_BOX_COLUMN_PARAM = "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
    @Override
    public int saveMsg(SendMsg msg) {
    	MessageInfo sendMsg = msg.getMessageInfo();
        StringBuilder sb = new StringBuilder();
        sb.append("insert into " + TABLE_MSG_BOX + "(" + TB_MSG_BOX_COLUMN + ")" + " values"+ TB_MSG_BOX_COLUMN_PARAM);
        Object[] objs = new Object[] { sendMsg.getMsgId(),msg.getActionInfo().getAppKey(),sendMsg.getChatId(),
                sendMsg.getFrom(), sendMsg.getTo(), sendMsg.getSubject(),
                StringUtils.defaultIfEmpty(sendMsg.getBody(), ""),
                sendMsg.isAttachmentMark() ? 1 : 0, "", EnumConstants.MESSAGE_NOT_READ, sendMsg.getSource(),
                sendMsg.getType(), sendMsg.getSubType(), 0 , new Timestamp(System.currentTimeMillis())};
        return this.getJdbcTemplate().update(sb.toString(), objs);
    }    
    
    @Override
    public Long checkChatStatus(Long chatId,String exceptUserId) {
    	StringBuffer sb = new StringBuffer();
		sb.append("select chme_chat_id from ");
		sb.append(TABLE_CHAT_USER);	
		Map<String, Object> paramMap = new HashMap<String, Object>();
		if(null == chatId){
			sb.append(" where chme_chat_id = :chatId and chme_user_account <> :exceptUserId and chme_status=0");
			paramMap.put("chatId", chatId);
		}else{
			sb.append(" where chme_user_account <> :exceptUserId and chme_status=0");
		}
		paramMap.put("exceptUserId", exceptUserId);
		try{
			return this.getNamedParameterJdbcTemplate().queryForLong(sb.toString(), paramMap);
		}catch(EmptyResultDataAccessException e){
			return null;
		}
    }

	@Override
	public List<AuthAccount> getTargetUser(long chatId, String exceptUserId, String deviceToken) {
		List<AuthAccount> result = null;
		StringBuffer sb = new StringBuffer();
		sb.append("select chme_user_account, auac_pn_token,auac_connect_type,auac_app_key,auac_device_token from ");
		sb.append(TABLE_CHAT_USER+" JOIN "+TABLE_AUTH_ACCOUNT);						
		sb.append(" on chme_user_account = auac_user_account");
		sb.append(" and auac_app_key = chme_app_key");
		sb.append(" where chme_chat_id = :chatId and ( chme_user_account <> :exceptUserId or auac_device_token <> :deviceToken ) ");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("chatId", chatId);
		paramMap.put("exceptUserId", exceptUserId);
		paramMap.put("deviceToken", deviceToken);
		try {
			result = this.getNamedParameterJdbcTemplate()
					.query(sb.toString(), paramMap, new RowMapper<AuthAccount>() {
						@Override
						public AuthAccount mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							AuthAccount userAccount = new AuthAccount();
							userAccount.setAccount(rs.getString("chme_user_account"));							
							userAccount.setPnToken(rs.getString("auac_pn_token"));
							userAccount.setConnectType(rs.getInt("auac_connect_type"));
							userAccount.setAppKey(rs.getString("auac_app_key"));
							userAccount.setDeviceToken(rs.getString("auac_device_token"));
							return userAccount;
						}
					});
			
		} catch (Exception e) {
			logger.error("Get chat authAccount error! chatId:" + chatId, e);
		}
		return result;
	}

	@Override
	public void addMsgQueue(List<AuthAccount> userGroup,MessageInfo messageInfo) {
		if(CollectionUtils.isEmpty(userGroup)){
			logger.info("target user is null");
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("insert into hichat.tb_msg_queue_r("
				+ "msqu_user_account,msqu_msg_id,msqu_type,msqu_create_time,msqu_app_key,msqu_chat_id,msqu_device_token)"
				+ " values(?,?,?,?,?,?,?)");
		List<Object[]> batchList = new ArrayList<Object[]>();
		for(AuthAccount mq : userGroup){
			Object[] objs = new Object[]{
					mq.getAccount(),messageInfo.getMsgId(),messageInfo.getType(), 
					new Timestamp(System.currentTimeMillis()),mq.getAppKey(),messageInfo.getChatId(),mq.getDeviceToken()==null?mq.getAccount():mq.getDeviceToken()};
			batchList.add(objs);
		}
		this.getJdbcTemplate().batchUpdate(sb.toString(), batchList);
    }

    @Override
    public void addAttachment(MessageInfo messageInfo,ActionInfo actionInfo) {
    	StringBuilder sb = new StringBuilder();
		sb.append("insert into hichat.tb_msg_attach_r("
				+ "msat_id,msat_name,msat_size,msat_type,msat_url,"
				+ "msat_md5,msat_create_time,msat_app_key)"
				+ " values(?,?,?,?,?,?,?,?)");
		AttachInfo attach = messageInfo.getAttachInfo();
		Object[] objs = new Object[]{messageInfo.getMsgId(),attach.getName(),attach.getSize(), attach.getType(),attach.getUrl(),
				attach.getMd5(),new Timestamp(System.currentTimeMillis()),actionInfo.getAppKey()};
		this.getJdbcTemplate().update(sb.toString(), objs);

    }
    
    @Override
	public List<MessageInfo> getNewMessages(String account, String deviceToken) {
		Map<String, Object> paramMap = new HashMap<String, Object>();

		String selectSql = "select msbo_id,msbo_type,msbo_subtype, msbo_chat_id,msbo_from,msbo_to,"
				+ "msbo_attach_mark,msbo_subject,msbo_content,msbo_source,msbo_create_time time,"
				+ "msat_id,msat_name,msat_size,msat_type,msat_md5,msat_content,msat_url,1 as unread "
				+ "from "
				+ "(select * from hichat.tb_msg_queue_r, hichat.tb_msg_box_r "
				+ "where msbo_id=msqu_msg_id and msqu_user_account=:msqu_user_account and msqu_device_token = :deviceToken"
				+ ") a left join hichat.tb_msg_attach_r b on a.msbo_id=b.msat_id";
		paramMap.put("msqu_user_account", account);
		paramMap.put("deviceToken", deviceToken);
		List<MessageInfo> list = this.getNamedParameterJdbcTemplate().query(
				selectSql, paramMap, new RowMapper<MessageInfo>() {
					@Override
					public MessageInfo mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						MessageInfo mi = new MessageInfo();
						mi.setMsgId(rs.getString("msbo_id"));
						mi.setChatId(rs.getLong("msbo_chat_id"));
						mi.setType(rs.getInt("msbo_type"));
						mi.setNotifyType(rs.getString("msbo_subtype"));
						mi.setUnread(rs.getInt("unread"));
						mi.setFrom(rs.getString("msbo_from"));
						mi.setTo(rs.getString("msbo_to"));
						mi.setSubject(rs.getString("msbo_subject"));
						mi.setBody(rs.getString("msbo_content"));
						mi.setSource(rs.getInt("msbo_source"));
						mi.setTime(rs.getTimestamp("time").getTime());
						mi.setAttachmentMark(rs.getBoolean("msbo_attach_mark"));
						if (mi.isAttachmentMark()
								&& rs.getString("msat_id") != null
								&& !rs.getString("msat_id").isEmpty()) {
							AttachInfo ai = new AttachInfo();
							ai.setType(rs.getInt("msat_type"));
							ai.setName(rs.getString("msat_name"));
							ai.setSize(rs.getInt("msat_size"));
							ai.setUrl(rs.getString("msat_url"));
							ai.setMd5(rs.getString("msat_md5"));
							ai.setAttachment(rs.getString("msat_content"));
							mi.setAttachInfo(ai);
						}
						return mi;
					}

				});
		return list;
	}
    
    @Override
    public List<MessageInfo> getNewMessages(String account, long chatId) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        
        String selectSql = "select msbo_id,msbo_type,msbo_subtype, msbo_chat_id,msbo_from,msbo_to,"
                + "msbo_attach_mark,msbo_subject,msbo_content,msbo_create_time time,"
                + "msat_id,msat_name,msat_size,msat_type,msat_md5,msat_content,msat_url "
                + "from "
                + "(select * from hichat.tb_msg_queue_r, hichat.tb_msg_box_r "
                + "where msbo_id=msqu_msg_id and msqu_user_account=:msqu_user_account and msqu_status = 0";
        if (chatId != 0) {
            selectSql += " and msbo_chat_id=:msbo_chat_id";
            paramMap.put("msbo_chat_id", chatId);
        }
        selectSql += ") a left join hichat.tb_msg_attach_r b on a.msbo_id=b.msat_id";
        paramMap.put("msqu_user_account", account);
        List<MessageInfo> list = this.getNamedParameterJdbcTemplate().query(selectSql, paramMap,
                new RowMapper<MessageInfo>() {
                    @Override
                    public MessageInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                        MessageInfo mi = new MessageInfo();
                        mi.setMsgId(rs.getString("msbo_id"));
                        mi.setChatId(rs.getLong("msbo_chat_id"));
                        mi.setType(rs.getInt("msbo_type"));
                        mi.setNotifyType(rs.getString("msbo_subtype"));
                        mi.setFrom(rs.getString("msbo_from"));
                        mi.setTo(rs.getString("msbo_to"));
                        mi.setSubject(rs.getString("msbo_subject"));
                        mi.setBody(rs.getString("msbo_content"));
                        mi.setTime(rs.getTimestamp("time").getTime());
                        mi.setAttachmentMark(rs.getBoolean("msbo_attach_mark"));
                        if (mi.isAttachmentMark() && rs.getString("msat_id") != null 
                                && !rs.getString("msat_id").isEmpty()) {
                            AttachInfo ai = new AttachInfo();
                            ai.setType(rs.getInt("msat_type"));
                            ai.setName(rs.getString("msat_name"));
                            ai.setSize(rs.getInt("msat_size"));
                            ai.setUrl(rs.getString("msat_url"));
                            ai.setMd5(rs.getString("msat_md5"));
                            ai.setAttachment(rs.getString("msat_content"));
                            mi.setAttachInfo(ai);
                        }
                        return mi;
                    }

                });
        return list;
    }

    @Override
    public int checkMsgHisQueue(String account) {
        String deleteSql = "delete hichat.tb_msg_his_queue_r "
                + "from hichat.tb_msg_queue_r,hichat.tb_msg_his_queue_r "
                + "where msqu_user_account=mhqu_user_account and msqu_msg_id=mhqu_msg_id "
                + "and mhqu_user_account=:mhqu_user_account";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("mhqu_user_account", account);
        return this.getNamedParameterJdbcTemplate().update(deleteSql, paramMap);
    }

    @Override
    public int msgInsertHisQueue(String account) {
        String insertSql = "insert into hichat.tb_msg_his_queue_r"
                + "(mhqu_user_account, mhqu_msg_id, mhqu_type, mhqu_create_time) "
                + "(select msqu_user_account, msqu_msg_id, msqu_type, msqu_create_time "
                + "from hichat.tb_msg_queue_r where msqu_user_account=:msqu_user_account)";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("msqu_user_account", account);
        return this.getNamedParameterJdbcTemplate().update(insertSql, paramMap);
    }

    @Override
    public int deleteMsgQueue(String account) {
        String deleteSql = "delete from hichat.tb_msg_queue_r "
                + "where msqu_user_account=:msqu_user_account";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("msqu_user_account", account);
        return this.getNamedParameterJdbcTemplate().update(deleteSql, paramMap);
    }
    

    @Override
    public void updateOnline(String account, byte userStateOnline,String appKey, String deviceToken) {
        String updateSql = "replace into " +  TABLE_AUTH_ACCOUNT + 
        		" set auac_user_state = :state,auac_connect_type= :connectType,auac_user_account = :account "+
        		" ,auac_device_token= :deviceToken,auac_app_key=:appKey,auac_terminal_type=:terminalType"+
        		" ,auac_create_time=:createTime";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("state", userStateOnline);
        paramMap.put("connectType", EnumConstants.CONNECT_TYPE_FROM_WEB);
        paramMap.put("account", account);
        paramMap.put("deviceToken", deviceToken);
        paramMap.put("appKey", appKey);
        paramMap.put("terminalType", EnumConstants.AUTH_ACCOUNT_TERMINALTYPE_WEB);
        paramMap.put("createTime", new Timestamp(System.currentTimeMillis()));
        this.getNamedParameterJdbcTemplate().update(updateSql, paramMap);
    }

    @Override
    public List<MessageInfo> getHistoryMessages(String account, HisMsgParam hisMsgParam) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String selectBoxSql = "select * from hichat.tb_msg_box_r where ";

        if (hisMsgParam.getChatId() > 0) {
        	//根据chatId历史记录
            selectBoxSql += "msbo_chat_id=:msbo_chat_id ";
            paramMap.put("msbo_chat_id", hisMsgParam.getChatId());        	            
        } else {
            selectBoxSql += "msbo_chat_id in (select chme_chat_id from  hichat.tb_chat_user_r"        				
                    + " where chme_user_account=:chme_user_account and chme_virtual_number=:destUserId)";
            paramMap.put("chme_user_account", account);
            paramMap.put("destUserId", hisMsgParam.getDestUserId());
        }
        if(hisMsgParam.getTime()> HisMsgParam.TIME_DEFAULT_VALUE){
        	selectBoxSql += " and unix_timestamp(msbo_create_time)<"+hisMsgParam.getTime();
        }
        selectBoxSql += " order by msbo_create_time ";
        if (hisMsgParam.getOrder() == HisMsgParam.ORDER_DESC) { // 逆序
            selectBoxSql += "desc ";
        } else {
            selectBoxSql += "asc ";
        }
        if(hisMsgParam.getOffset()== HisMsgParam.OFFSET_DEFAULT_VALUE){
        	selectBoxSql += "limit "+ hisMsgParam.getLimit();
        }else{
        	selectBoxSql += "limit " + hisMsgParam.getOffset() + ", " + hisMsgParam.getLimit();
        }
        String selectSql = "select msbo_id,msbo_type,msbo_chat_id,msbo_from,msbo_to,"
                + "msbo_attach_mark,msbo_subject,msbo_content,msbo_source,msbo_create_time time,"
                + "msat_id,msat_name,msat_size,msat_type,msat_md5,msat_content,msat_url "
                + "from (" + selectBoxSql + ") a "
                + "left join hichat.tb_msg_attach_r b on a.msbo_id=b.msat_id";
        
        List<MessageInfo> list = this.getNamedParameterJdbcTemplate().query(selectSql, paramMap,
                new RowMapper<MessageInfo>() {
                    @Override
                    public MessageInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                        MessageInfo mi = new MessageInfo();
                        mi.setMsgId(rs.getString("msbo_id"));
                        mi.setChatId(rs.getLong("msbo_chat_id"));
                        mi.setType(rs.getInt("msbo_type"));
                        mi.setFrom(rs.getString("msbo_from"));
                        mi.setTo(rs.getString("msbo_to"));
                        mi.setSubject(rs.getString("msbo_subject"));
                        mi.setBody(rs.getString("msbo_content"));
                        mi.setSource(rs.getInt("msbo_source"));
                        mi.setTime(rs.getTimestamp("time").getTime());
                        mi.setAttachmentMark(rs.getBoolean("msbo_attach_mark"));
                        if (mi.isAttachmentMark() && rs.getString("msat_id") != null 
                                && !rs.getString("msat_id").isEmpty()) {
                            AttachInfo ai = new AttachInfo();
                            ai.setType(rs.getInt("msat_type"));
                            ai.setName(rs.getString("msat_name"));
                            ai.setSize(rs.getInt("msat_size"));
                            ai.setUrl(rs.getString("msat_url"));
                            ai.setMd5(rs.getString("msat_md5"));
                            ai.setAttachment(rs.getString("msat_content"));
                            mi.setAttachInfo(ai);
                        }
                        return mi;
                    }

                });
        return list;
    }

	@Override
	public PatientClientStatus checkOldPatient(String userId) {
		String checkSql = "select count(*) hasdUser, isnull(auac_pn_token) oldVersion from " + TABLE_AUTH_ACCOUNT + " where auac_user_account = :userId and auac_user_type = 0";
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("userId", userId);
		PatientClientStatus result = this.getNamedParameterJdbcTemplate().queryForObject(checkSql, paramMap,  new RowMapper<PatientClientStatus>(){
			@Override
			public PatientClientStatus mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				PatientClientStatus result = new PatientClientStatus();
				result.setHasdUser(rs.getBoolean("hasdUser"));
				result.setOldVersion(rs.getBoolean("oldVersion"));
				return result;
			}
		});
		return result;
	}

	@Override
	public void updateMsgQueueState(List<String> queueMsgIdList,String account, int status) {
		 String update = "update hichat.tb_msg_queue_r set msqu_status = :status"
	                + " where msqu_user_account=:account and msqu_msg_id in (:queueMsgIdList)";
	        Map<String, Object> paramMap = new HashMap<String, Object>();
	        paramMap.put("account", account);
	        paramMap.put("status", status);
	        paramMap.put("queueMsgIdList", queueMsgIdList);
	        this.getNamedParameterJdbcTemplate().update(update, paramMap);
	}

	@Override
	public void msgInsertHisQueue(List<String> msgIdList, String account, String deviceToken) {
		 String insertSql = "insert into hichat.tb_msg_his_queue_r"
	                + "(mhqu_user_account,mhqu_app_key, mhqu_msg_id, mhqu_device_token, mhqu_type, mhqu_create_time) "
	                + "(select msqu_user_account,msqu_app_key,msqu_msg_id, msqu_device_token, msqu_type, msqu_create_time "
	                + "from hichat.tb_msg_queue_r where msqu_user_account=:account and msqu_device_token = :deviceToken and msqu_msg_id in (:msgIdList))";
	        Map<String, Object> paramMap = new HashMap<String, Object>();
	        paramMap.put("account", account);
	        paramMap.put("msgIdList", msgIdList);
	        paramMap.put("deviceToken", deviceToken);
	        this.getNamedParameterJdbcTemplate().update(insertSql, paramMap);
	}

	@Override
	public void deleteMsgQueue(List<String> msgIdList, String account, String deviceToken) {
		  String deleteSql = "delete from hichat.tb_msg_queue_r "
	                + "where msqu_user_account=:account and msqu_device_token = :deviceToken and msqu_msg_id in (:msgIdList)";
	        Map<String, Object> paramMap = new HashMap<String, Object>();
	        paramMap.put("account", account);
	        paramMap.put("msgIdList", msgIdList);
	        paramMap.put("deviceToken", deviceToken);
	        this.getNamedParameterJdbcTemplate().update(deleteSql, paramMap);
	}

    @Override
    public List<MessageInfo> getAllHistoryMessages(String account, HisMsgParam hisMsgParam) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        
        String selectChatSql = "select distinct chme_chat_id from hichat.tb_chat_user_r "
                + "where chme_user_account=:chme_user_account";
        paramMap.put("chme_user_account", account);
        
        String selectBoxSql = "select A.* from hichat.tb_msg_box_r A,hichat.tb_msg_his_queue_r "
                + "WHERE (SELECT COUNT(msbo_chat_id) FROM hichat.tb_msg_box_r,hichat.tb_msg_his_queue_r "
                + "WHERE msbo_chat_id in (" + selectChatSql + ") "
                + "and msbo_chat_id = A.msbo_chat_id and msbo_create_time";
        if (hisMsgParam.getOrder() == 0) { // 逆序
            selectBoxSql += " > ";
        } else {
            selectBoxSql += " < ";
        }
        selectBoxSql += "A.msbo_create_time  and msbo_id=mhqu_msg_id and msbo_type=0 "
                + ") < " + hisMsgParam.getLimit()
                + " and msbo_chat_id in (" + selectChatSql + ") "
                + "and msbo_id=mhqu_msg_id and msbo_type=0 ORDER BY A.msbo_create_time desc ";

        String selectSql = "select msbo_id,msbo_type,msbo_chat_id,msbo_from,msbo_to,"
                + "msbo_attach_mark,msbo_subject,msbo_content,msbo_create_time time,"
                + "msat_id,msat_name,msat_size,msat_type,msat_md5,msat_content,msat_url "
                + "from (" + selectBoxSql + ") a "
                + "left join hichat.tb_msg_attach_r b on a.msbo_id=b.msat_id";
        
        List<MessageInfo> list = this.getNamedParameterJdbcTemplate().query(selectSql, paramMap,
                new RowMapper<MessageInfo>() {
                    @Override
                    public MessageInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                        MessageInfo mi = new MessageInfo();
                        mi.setMsgId(rs.getString("msbo_id"));
                        mi.setChatId(rs.getLong("msbo_chat_id"));
                        mi.setType(rs.getInt("msbo_type"));
                        mi.setFrom(rs.getString("msbo_from"));
                        mi.setTo(rs.getString("msbo_to"));
                        mi.setSubject(rs.getString("msbo_subject"));
                        mi.setBody(rs.getString("msbo_content"));
                        mi.setTime(rs.getTimestamp("time").getTime());
                        mi.setAttachmentMark(rs.getBoolean("msbo_attach_mark"));
                        if (mi.isAttachmentMark() && rs.getString("msat_id") != null 
                                && !rs.getString("msat_id").isEmpty()) {
                            AttachInfo ai = new AttachInfo();
                            ai.setType(rs.getInt("msat_type"));
                            ai.setName(rs.getString("msat_name"));
                            ai.setSize(rs.getInt("msat_size"));
                            ai.setUrl(rs.getString("msat_url"));
                            ai.setMd5(rs.getString("msat_md5"));
                            ai.setAttachment(rs.getString("msat_content"));
                            mi.setAttachInfo(ai);
                        }
                        return mi;
                    }

                });
        return list;
    }
    
    @Override
	public void updateMsgReadStatus(List<String> recvMsgList, byte msgReaded) {
		String update = "update  hichat.tb_msg_box_r set msbo_is_read = :msgReaded "
				+ " where msbo_id in (:recvMsgList)";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("msgReaded", msgReaded);
		paramMap.put("recvMsgList", recvMsgList);
		this.getNamedParameterJdbcTemplate().update(update, paramMap);
	}

	@Override
	public void addMsgQueue(DispatchChatInfo oldestDic, String userId, String appKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into hichat.tb_msg_queue_r("
				+ "msqu_user_account,msqu_msg_id,msqu_type,msqu_create_time,msqu_app_key,msqu_chat_id,msqu_device_token)"
				+ " select :targetAccount, msbo_id, :msgType, :createTime, :appKey, :chatId, :targetAccount"
				+ " from hichat.tb_chat_user_r , hichat.tb_msg_box_r WHERE chme_chat_id = :chatId  "
				+ " and chme_user_account = :targetAccount and chme_chat_id = msbo_chat_id "
				+ " AND msbo_from != chme_virtual_number");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("targetAccount", userId);
		paramMap.put("msgType", EnumConstants.MESSAGE_TYPE_SIMPLE);
		paramMap.put("createTime", new Timestamp(System.currentTimeMillis()));
		paramMap.put("appKey", appKey);
		paramMap.put("chatId", oldestDic.getChatId());
		this.getNamedParameterJdbcTemplate().update(sb.toString(), paramMap);
	}

}
