package com.hesine.hichat.access.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.dao.ChatListDAO;
import com.hesine.hichat.model.AccountInfo;
import com.hesine.hichat.model.AttachInfo;
import com.hesine.hichat.model.ChatInfo;
import com.hesine.hichat.model.MessageInfo;
import com.hesine.hichat.model.ReqParam;

@Component("chatListDAO")
public class ChatListDAOImpl extends BaseDAO implements ChatListDAO {

	@Override
	public List<ChatInfo> getChatIdByHospitals(String hospitalIds) {
		String selectSql = "select distinct chme_chat_id "
				+ "from hichat.tb_chat_user_r,hi_auth.tb_user_extend_r "
				+ "where usex_user_account = chme_user_account and usex_group_id in ( "
				+ hospitalIds + " )";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<ChatInfo> list = this.getNamedParameterJdbcTemplate().query(
				selectSql, paramMap, new RowMapper<ChatInfo>() {

					@Override
					public ChatInfo mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ChatInfo csi = new ChatInfo();
						csi.setChatId(rs.getLong("chme_chat_id"));
						return csi;
					}

				});

		return list;
	}

	@Override
	public List<ChatInfo> getChatIdByAccounts(String Accounts) {
		String selectSql = "select distinct chme_chat_id from hichat.tb_chat_user_r "
				+ "where chme_user_account in ( " + Accounts + " )";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<ChatInfo> list = this.getNamedParameterJdbcTemplate().query(
				selectSql, paramMap, new RowMapper<ChatInfo>() {

					@Override
					public ChatInfo mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ChatInfo csi = new ChatInfo();
						csi.setChatId(rs.getLong("chme_chat_id"));
						return csi;
					}

				});

		return list;
	}

	@Override
	public int getChatUnreadNum(long chatId) {
		String selectSql = "select count(*) from tb_msg_box_r "
				+ "where msbo_chat_id=:msbo_chat_id and msbo_type=0 and msbo_is_read=0";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("msbo_chat_id", chatId);
		return this.getNamedParameterJdbcTemplate().queryForInt(selectSql,
				paramMap);
	}

	@Override
	public int getChatMsgTotal(long chatId) {
		String selectSql = "select count(*) from hichat.tb_msg_box_r "
				+ "where msbo_chat_id=:msbo_chat_id and msbo_type=0";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("msbo_chat_id", chatId);
		return this.getNamedParameterJdbcTemplate().queryForInt(selectSql,
				paramMap);
	}

	@Override
	public MessageInfo getChatLastMsg(long chatId) {
		String selectSql = "select msbo_id,msbo_type,msbo_chat_id,msbo_from,msbo_to,"
				+ "msbo_attach_mark,msbo_subject,msbo_content,msbo_create_time time,"
				+ "msat_id,msat_name,msat_size,msat_type,msat_md5,msat_content,msat_url from "
				+ "( select * from hichat.tb_msg_box_r "
				+ "where msbo_chat_id=:msbo_chat_id and msbo_type=0 order by msbo_create_time desc limit 1 ) a "
				+ "left join hichat.tb_msg_attach_r b on a.msbo_id=b.msat_id";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("msbo_chat_id", chatId);
		MessageInfo messageInfo = this.getNamedParameterJdbcTemplate().query(
				selectSql, paramMap, new ResultSetExtractor<MessageInfo>() {

					@Override
					public MessageInfo extractData(ResultSet rs)
							throws SQLException, DataAccessException {
						rs.next();
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

		return messageInfo;
	}

	@Override
	public long getChatLastTime(long chatId) {
		String selectSql = "select unix_timestamp(msbo_create_time) create_time "
				+ "from hichat.tb_msg_box_r "
				+ "where msbo_chat_id=:msbo_chat_id "
				+ "and msbo_type=0 order by msbo_create_time desc limit 1";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("msbo_chat_id", chatId);
		return this.getNamedParameterJdbcTemplate().queryForLong(selectSql,
				paramMap);
	}

	@Override
	public List<MessageInfo> getLastDaysMsgs(long chatId, int number) {
		String selectSql = "select msbo_id,msbo_type,msbo_chat_id,msbo_from,msbo_to,"
				+ "msbo_attach_mark,msbo_subject,msbo_content,msbo_create_time time,"
				+ "msat_id,msat_name,msat_size,msat_type,msat_md5,msat_content,msat_url from "
				+ "( select * from hichat.tb_msg_box_r "
				+ "where msbo_chat_id=:msbo_chat_id and "
				+ "msbo_type=0 and DATE(msbo_create_time) >= DATE(DATE_SUB(now(),INTERVAL "
				+ number
				+ " day)) ) a left join hichat.tb_msg_attach_r b on a.msbo_id=b.msat_id";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("msbo_chat_id", chatId);
		List<MessageInfo> list = this.getNamedParameterJdbcTemplate().query(
				selectSql, paramMap, new RowMapper<MessageInfo>() {
					@Override
					public MessageInfo mapRow(ResultSet rs, int rowNum)
							throws SQLException {
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
	public List<ChatInfo> getChatList(String appKey, ReqParam reqParam) {
		String selectSql = "select a.*,c.* from ("
				+ "select chat_id,chme_create_time,chme_update_time,"
				+ "chme_status,count(chat_id) totalCnt, max(msbo_create_time) lastMsgTime "
				+ " from ("
				+ "select t1.chme_chat_id chat_id,min(chme_create_time) chme_create_time,max(chme_update_time) chme_update_time,chme_status from hichat.tb_chat_user_r t1, "
				+ " ( "
				+ " SELECT DISTINCT chme_chat_id "
				+ " FROM hichat.tb_chat_user_r "
				+ " WHERE "
				+ " chme_app_key = :appKey"
				+ " and chme_type=:chatType "
				+ (reqParam.getChatFlag() == 2 ? ""
						: " and chme_status = :chatStatus ")
				+ (StringUtils.isNotEmpty(reqParam.getStartDate()) ? " AND chme_create_time >= DATE(:startDate)	"
						: "")
				+ (StringUtils.isNotEmpty(reqParam.getEndDate()) ? " AND chme_create_time <= DATE(:endDate)	"
						: "")
				+ (StringUtils.isNotEmpty(reqParam.getUserId()) ? " and chme_user_account in  ("
						+ " select usac_user_account "
						+ " from hiauth.tb_user_account_r "
						+ " where chme_user_account = :requestUserId"
						+ " and chme_user_account = usac_user_account "
						+ " and usac_user_type in (2,3) " + " ) "
						: "")
				+ (StringUtils.isNotEmpty(reqParam.getServiceNickName()) ? " and chme_user_account in  ("
						+ " select usac_user_account "
						+ " from hiauth.tb_user_account_r "
						+ " where usac_nickname = :serviceNickName"
						+ " and chme_user_account = usac_user_account "
						+ " and usac_user_type in (2,3) " + " ) "
						: "")
				+ (StringUtils.isNotEmpty(reqParam.getCustomerNickName()) ? " and chme_chat_id in ( "
						+ " SELECT chme_chat_id  FROM hichat.tb_chat_user_r a, hiauth.tb_user_account_r"
						+ " WHERE  usac_user_type = 0 and usac_nickname  = :customerNickName"
						+ " and chme_user_account = usac_user_account )"
						: "")
				+ " order by chme_update_time desc limit :startIdx, :increment "
				+ " ) t2 "
				+ " where t1.chme_chat_id = t2.chme_chat_id "
				+ " GROUP BY t1.chme_chat_id, chme_status"
				+ " ) chat_list left join hichat.tb_msg_box_r on msbo_chat_id=chat_id "
				+ " group by chat_id) a "
				+ " join hichat.tb_chat_user_r b"
				+ " on a.chat_id=b.chme_chat_id"
				+ " join hiauth.tb_user_account_r c on b.chme_user_account=c.usac_user_account"
				+ " and b.chme_app_key = c.usac_app_key "
				+ " order by lastMsgTime desc";

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("appKey", appKey);
		param.put("chatType", reqParam.getChatType());
		param.put("chatStatus", reqParam.getChatFlag());
		param.put("startDate", reqParam.getStartDate());
		param.put("endDate", reqParam.getEndDate());
		param.put("customerNickName", reqParam.getCustomerNickName());
		param.put("serviceNickName", reqParam.getServiceNickName());
		param.put("requestUserId", reqParam.getUserId());
		param.put("startIdx", reqParam.getPageSize()
				* (reqParam.getCurPage() - 1));
		param.put("increment", reqParam.getPageSize());

		List<ChatInfo> list = this.getNamedParameterJdbcTemplate().query(
				selectSql, param, new ResultSetExtractor<List<ChatInfo>>() {
					@Override
					public List<ChatInfo> extractData(ResultSet rs)
							throws SQLException, DataAccessException {
						ChatInfo chatInfo = null;
						List<ChatInfo> list = new ArrayList<ChatInfo>();
						Map<Long, ChatInfo> chatIdMap = new HashMap<Long, ChatInfo>();
						while (rs.next()) {
							AccountInfo accountInfo = new AccountInfo();
							accountInfo.setNickName(rs
									.getString("usac_nickname"));
							accountInfo.setUserId(rs
									.getString("usac_user_account"));
							accountInfo.setUserType(rs.getInt("usac_user_type"));
							if (chatIdMap.containsKey(rs.getLong("chat_id"))) {
								chatInfo = chatIdMap.get(rs.getLong("chat_id"));
								chatInfo.getMemberList().add(accountInfo);
							} else {
								chatInfo = new ChatInfo();
								chatInfo.setChatId(rs.getLong("chat_id"));
								chatInfo.setChatTotalMsg(rs.getInt("totalCnt"));
								chatInfo.setChatStatus(rs.getInt("chme_status"));
								chatInfo.setCreateTime(rs.getTimestamp(
										"chme_create_time").getTime());
								chatInfo.setChatLastTime(rs.getTimestamp(
										"lastMsgTime").getTime());
								List<AccountInfo> accountList = new ArrayList<AccountInfo>();
								accountList.add(accountInfo);
								chatInfo.setMemberList(accountList);
								chatIdMap.put(rs.getLong("chat_id"), chatInfo);
								MessageInfo chatLastMsg = new MessageInfo();
								chatLastMsg.setTime(rs.getTimestamp(
										"lastMsgTime").getTime());
								chatInfo.setChatLastMsg(chatLastMsg);
								list.add(chatInfo);
							}
						}
						return list;
					}
				});

		return list;
	}

	@Override
	public int getHisChatCnt(String appKey, ReqParam reqParam) {
		String select = " SELECT count(DISTINCT chme_chat_id) "
				+ " FROM hichat.tb_chat_user_r "
				+ " WHERE "
				+ " chme_app_key = :appKey"
				+ " and chme_type=:chatType "
				+ (reqParam.getChatFlag() == 2 ? ""
						: " and chme_status = :chatStatus ")
				+ (StringUtils.isNotEmpty(reqParam.getStartDate()) ? " AND chme_create_time >= DATE(:startDate)	"
						: "")
				+ (StringUtils.isNotEmpty(reqParam.getEndDate()) ? " AND chme_create_time <= DATE(:endDate)	"
						: "")
				+ (StringUtils.isNotEmpty(reqParam.getUserId()) ? " and chme_user_account in  ("
						+ " select usac_user_account "
						+ " from hiauth.tb_user_account_r "
						+ " where chme_user_account = :requestUserId"
						+ " and chme_user_account = usac_user_account "
						+ " and usac_user_type in (2,3) " + " ) "
						: "")
				+ (StringUtils.isNotEmpty(reqParam.getServiceNickName()) ? " and chme_user_account in  ("
						+ " select usac_user_account "
						+ " from hiauth.tb_user_account_r "
						+ " where usac_nickname = :serviceNickName"
						+ " and chme_user_account = usac_user_account "
						+ " and usac_user_type in (2,3) " + " ) "
						: "")
				+ (StringUtils.isNotEmpty(reqParam.getCustomerNickName()) ? " and chme_chat_id in ( "
						+ " SELECT chme_chat_id  FROM hichat.tb_chat_user_r a, hiauth.tb_user_account_r"
						+ " WHERE  usac_user_type = 0 and usac_nickname  = :customerNickName"
						+ " and chme_user_account = usac_user_account )"
						: "");
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("appKey", appKey);
		param.put("chatType", reqParam.getChatType());
		param.put("chatStatus", reqParam.getChatFlag());
		param.put("startDate", reqParam.getStartDate());
		param.put("endDate", reqParam.getEndDate());
		param.put("customerNickName", reqParam.getCustomerNickName());
		param.put("serviceNickName", reqParam.getServiceNickName());
		param.put("requestUserId", reqParam.getUserId());
		return this.getNamedParameterJdbcTemplate().queryForInt(select, param);
	}

}
