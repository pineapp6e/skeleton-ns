package com.hesine.hichat.access.dao.impl;

import static com.hesine.hichat.access.common.EnumConstants.CHAT_STATUS_OPEN;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.dao.ChatOperationBAO;
import com.hesine.hichat.access.model.SettingInfo;
import com.hesine.hichat.model.AccountInfo;
import com.hesine.hichat.model.ChatInfo;
import com.hesine.hichat.model.CsChatSummary;
import com.hesine.hichat.model.DispatchChatInfo;
import com.hesine.hichat.model.request.Base;
import com.hesine.hichat.model.request.SendMsg;
import com.hesine.hichat.model.response.CustomerInfo;

@Component("chatOperationBAO")
public class ChatOperationBAOImpl extends BaseDAO implements ChatOperationBAO {

	@Override
	public int checkChatUser(long chatId, String userId) {
		String selectSql = "select count(*) from hichat.tb_chat_user_r "
				+ "where chme_chat_id=:chme_chat_id and chme_user_account=:chme_user_account";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("chme_chat_id", chatId);
		paramMap.put("chme_user_account", userId);
		return this.getNamedParameterJdbcTemplate().queryForInt(selectSql,
				paramMap);
	}

	@Override
	public String getOtherChatUser(long chatId, String userId) {
		String selectSql = "select chme_user_account from hichat.tb_chat_user_r "
				+ "where chme_chat_id=:chme_chat_id and chme_user_account<>:chme_user_account";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("chme_chat_id", chatId);
		paramMap.put("chme_user_account", userId);
		return this.getNamedParameterJdbcTemplate().queryForObject(selectSql,
				paramMap, String.class);
	}

	@Override
	public int insertChatUser(long chatId, String account) {
		String insertSql = "insert into hichat.tb_chat_user_r"
				+ "(chme_chat_id, chme_user_account, chme_status, chme_create_time) "
				+ "values (:chme_chat_id, :chme_user_account, :chme_status, :chme_create_time) ";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("chme_chat_id", chatId);
		paramMap.put("chme_user_account", account);
		paramMap.put("chme_status", CHAT_STATUS_OPEN);
		paramMap.put("chme_create_time",
				new Timestamp(System.currentTimeMillis()));
		return this.getNamedParameterJdbcTemplate().update(insertSql, paramMap);
	}

	@Override
	public long createChat(SendMsg msg, long chatId, String userAccount,
			String virtualNumber, int status) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into hichat.tb_chat_user_r"
				+ "(chme_chat_id,chme_user_account,chme_app_key,chme_type,chme_status,chme_create_time,chme_virtual_number)"
				+ " values(?,?,?,?,?,?,?)");

		Object[] objs = new Object[] { chatId, userAccount,
				msg.getActionInfo().getAppKey(),
				msg.getMessageInfo().getType(), status,
				new Timestamp(System.currentTimeMillis()), virtualNumber };
		int result = this.getJdbcTemplate().update(sb.toString(), objs);
		return result;
	}

	@Override
	public int checkAuthAccount(String account) {
		String selectSql = "select count(*) from hiauth.tb_auth_account_r where auac_user_account = :auac_user_account";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("auac_user_account", account);
		return this.getNamedParameterJdbcTemplate().queryForInt(selectSql,
				paramMap);
	}

	@Override
	public int insertAuthAccount(String account, int userType) {
		String insertSql = "insert into hiauth.tb_auth_account_r"
				+ "(auac_user_account, auac_user_state, auac_user_type, auac_create_time) "
				+ "values (:auac_user_account, :auac_user_state, :auac_user_type, :auac_create_time)";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("auac_user_account", account);
		paramMap.put("auac_user_state", 1);
		paramMap.put("auac_user_type", userType);
		paramMap.put("auac_create_time",
				new Timestamp(System.currentTimeMillis()));
		return this.getNamedParameterJdbcTemplate().update(insertSql, paramMap);
	}

	@Override
	public ChatInfo getChatStatusInfo(String userId, String serviceNumber) {
		StringBuilder select = new StringBuilder();
		select.append("select max(chme_chat_id) chme_chat_id, chme_status from hichat.tb_chat_user_r");
		select.append(" where chme_user_account = ? and chme_virtual_number = ? and ( chme_status=0 or chme_status=2) ");
		ChatInfo chatStatusInfo = null;
		try {
			chatStatusInfo = this.getJdbcTemplate().queryForObject(
					select.toString(), new RowMapper<ChatInfo>() {
						@Override
						public ChatInfo mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							ChatInfo chatStatusInfo = new ChatInfo();
							chatStatusInfo.setChatId(rs.getLong("chme_chat_id"));
							chatStatusInfo.setChatStatus(rs
									.getInt("chme_status"));
							return chatStatusInfo;
						}
					}, userId, serviceNumber);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		return chatStatusInfo;
	}

	public List<CustomerInfo> getCustomerList(Base msg) {
		StringBuilder select = new StringBuilder();
		select.append("select usac_user_account,usac_nickname,auac_user_state from hiauth.tb_user_account_r a");
		select.append(" left join hiauth.tb_auth_account_r b on a.usac_user_account=b.auac_user_account");
		select.append(" where usac_app_key=? and usac_app_key = auac_app_key and usac_user_type>=2");
		List<CustomerInfo> customerList = null;
		try {
			customerList = this.getJdbcTemplate().query(select.toString(),
					new RowMapper<CustomerInfo>() {
						@Override
						public CustomerInfo mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							CustomerInfo customerInfo = new CustomerInfo();
							AccountInfo accountInfo = new AccountInfo();
							accountInfo.setUserId(rs
									.getString("usac_user_account"));
							accountInfo.setNickName(rs
									.getString("usac_nickname"));
							accountInfo.setUserState(rs
									.getInt("auac_user_state"));
							customerInfo.setAccountInfo(accountInfo);

							return customerInfo;
						}
					}, msg.getActionInfo().getAppKey());
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		return customerList;
	}

	@Override
	public int deleteChatUser(long chatId, String account) {
		String deleteSql = "delete from hichat.tb_chat_user_r "
				+ "where chme_chat_id=:chme_chat_id and chme_user_account=:chme_user_account";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("chme_chat_id", chatId);
		paramMap.put("chme_user_account", account);
		return this.getNamedParameterJdbcTemplate().update(deleteSql, paramMap);
	}

	@Override
	public int updateChatStatus(long chatId, int chatStatus) {
		String updateSql = "update hichat.tb_chat_user_r set chme_status=:chme_status "
				+ "where chme_chat_id=:chme_chat_id";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("chme_chat_id", chatId);
		paramMap.put("chme_status", chatStatus);
		return this.getNamedParameterJdbcTemplate().update(updateSql, paramMap);

	}

	@Override
	public int updateChatUser(long chatId, String oldAccount, String newAccount) {
		String updateSql = "update hichat.tb_chat_user_r set chme_user_account=:new_user_account "
				+ "where chme_chat_id=:chme_chat_id and chme_user_account=:old_user_account";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("chme_chat_id", chatId);
		paramMap.put("old_user_account", oldAccount);
		paramMap.put("new_user_account", newAccount);
		return this.getNamedParameterJdbcTemplate().update(updateSql, paramMap);
	}

	@Override
	public int checkUserExtend(String account, String groupId) {
		String selectSql = "select count(*) from hiauth.tb_user_extend_r "
				+ "where usex_user_account=:usex_user_account and usex_group_id=:usex_group_id";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("usex_user_account", account);
		paramMap.put("usex_group_id", groupId);
		return this.getNamedParameterJdbcTemplate().queryForInt(selectSql,
				paramMap);
	}

	@Override
	public int insertUserExtend(String account, String groupId) {
		String insertSql = "insert into hiauth.tb_user_extend_r"
				+ "(usex_user_account, usex_group_id, usex_create_time) "
				+ "values (:usex_user_account, :usex_group_id, :usex_create_time)";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("usex_user_account", account);
		paramMap.put("usex_group_id", groupId);
		paramMap.put("usex_create_time",
				new Timestamp(System.currentTimeMillis()));
		return this.getNamedParameterJdbcTemplate().update(insertSql, paramMap);
	}

	@Override
	public int checkGroup(String hospitalId) {
		String selectSql = "select count(*) from hiauth.tb_group_r "
				+ "where grop_group_id=:grop_group_id";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("grop_group_id", hospitalId);
		return this.getNamedParameterJdbcTemplate().queryForInt(selectSql,
				paramMap);
	}

	@Override
	public int insertGroup(String hospitalId) {
		String insertSql = "insert into hiauth.tb_group_r"
				+ "(grop_group_id, grop_create_time) "
				+ "values (:grop_group_id, :grop_create_time)";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("grop_group_id", hospitalId);
		paramMap.put("grop_create_time",
				new Timestamp(System.currentTimeMillis()));
		return this.getNamedParameterJdbcTemplate().update(insertSql, paramMap);
	}

	@Override
	public Map<Integer, SettingInfo> getSettingInfo(String appKey) {
		String selectSql = "select * from hiauth.tb_setting_info_r where sein_app_key='"
				+ appKey + "'";
		Map<String, Object> param = new HashMap<String, Object>();
		Map<Integer, SettingInfo> settingInfoMap = this
				.getNamedParameterJdbcTemplate().query(selectSql, param,
						new ResultSetExtractor<Map<Integer, SettingInfo>>() {
							@Override
							public Map<Integer, SettingInfo> extractData(
									ResultSet rs) throws SQLException,
									DataAccessException {
								Map<Integer, SettingInfo> settingInfoMap = new HashMap<Integer, SettingInfo>();
								while (rs.next()) {
									SettingInfo settingInfo = new SettingInfo();
									settingInfo.setContent(rs
											.getString("sein_content"));
									settingInfo.setIsSelected(rs
											.getInt("sein_selected"));
									settingInfo.setType(rs
											.getInt("sein_reg_type"));
									settingInfoMap.put(settingInfo.getType(),
											settingInfo);
								}
								return settingInfoMap;
							}
						});

		return settingInfoMap;
	}

	@Override
	public void createChat(String userId, String appKey, long chatId,
			String serviceNumber, int chatStatus) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into hichat.tb_chat_user_r"
				+ "(chme_chat_id,chme_user_account,chme_app_key,chme_type,chme_status,chme_create_time,chme_virtual_number)"
				+ " values(?,?,?,?,?,?,?)");

		Object[] objs = new Object[] { chatId, userId, appKey, 0, chatStatus,
				new Timestamp(System.currentTimeMillis()), serviceNumber };
		this.getJdbcTemplate().update(sb.toString(), objs);

	}

	@Override
	public int getInchatCnt(String csAccount) {
		String selectSql = "select count(*) from hichat.tb_chat_user_r "
				+ "where chme_user_account = :csAccount and chme_type=0 and chme_status = 0";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("csAccount", csAccount);
		return this.getNamedParameterJdbcTemplate().queryForInt(selectSql,
				paramMap);
	}

	@Override
	public Map<String, List<DispatchChatInfo>> getDispatchChats() {
		StringBuilder select = new StringBuilder();
		select.append("select chme_chat_id,chme_user_account,chme_create_time,chme_app_key from hichat.tb_chat_user_r ");
		select.append(" where chme_type = 0 and chme_status = 2 order by chme_app_key, chme_create_time asc");
		Map<String, List<DispatchChatInfo>> resultMap = null;
		try {
			resultMap = this
					.getJdbcTemplate()
					.query(select.toString(),
							new ResultSetExtractor<Map<String, List<DispatchChatInfo>>>() {
								@Override
								public Map<String, List<DispatchChatInfo>> extractData(
										ResultSet rs) throws SQLException {
									Map<String, List<DispatchChatInfo>> resultMap = new HashMap<String, List<DispatchChatInfo>>();

									while (rs.next()) {
										String appKey = rs
												.getString("chme_app_key");
										DispatchChatInfo dci = new DispatchChatInfo();
										dci.setChatId(rs
												.getLong("chme_chat_id"));
										dci.setCreateTime(rs.getTimestamp(
												"chme_create_time").getTime());
										dci.setUserId(rs
												.getString("chme_user_account"));
										if (resultMap.containsKey(appKey)) {
											resultMap.get(appKey).add(dci);
										} else {
											List<DispatchChatInfo> list = new ArrayList<DispatchChatInfo>();
											list.add(dci);
											resultMap.put(appKey, list);
										}
									}
									return resultMap;
								}
							});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultMap;
	}

	@Override
	public Map<String, Set<String>> getChatPersonMapByAppkey(
			final boolean filterByPerson) {
		StringBuilder select = new StringBuilder();
		select.append(" SELECT chme_app_key, "
				+ (filterByPerson ? "chme_user_account" : "chme_chat_id")
				+ "  from hichat.tb_chat_user_r , hiauth.tb_user_account_r");
		select.append(" where usac_user_type = 0 and usac_user_account = chme_user_account ");
		select.append(" and DATE(chme_create_time) = DATE(now())");
		select.append(" group by chme_app_key,  ");
		select.append((filterByPerson ? "chme_user_account" : "chme_chat_id"));
		Map<String, Set<String>> resultMap = null;
		try {
			resultMap = this.getJdbcTemplate().query(select.toString(),
					new ResultSetExtractor<Map<String, Set<String>>>() {
						@Override
						public Map<String, Set<String>> extractData(ResultSet rs)
								throws SQLException {
							Map<String, Set<String>> resultMap = new HashMap<String, Set<String>>();

							while (rs.next()) {
								String appKey = rs.getString("chme_app_key");

								String value = (filterByPerson ? rs
										.getString("chme_user_account") : rs
										.getString("chme_chat_id"));
								if (resultMap.containsKey(appKey)) {
									resultMap.get(appKey).add(value);
								} else {
									Set<String> userSet = new HashSet<String>();
									userSet.add(value);
									resultMap.put(appKey, userSet);
								}
							}
							return resultMap;
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultMap;
	}

	@Override
	public Map<? extends String, ? extends Integer> getMessageCntDay() {
		StringBuilder select = new StringBuilder();
		select.append(" SELECT msbo_app_key, count(msbo_id) as msgCnt");
		select.append(" FROM  hichat.tb_msg_box_r ");
		select.append(" WHERE DATE(msbo_create_time) = DATE(now())");
		select.append(" GROUP BY msbo_app_key ");
		Map<String, Integer> resultMap = null;
		resultMap = this.getJdbcTemplate().query(select.toString(),
				new ResultSetExtractor<Map<String, Integer>>() {
					@Override
					public Map<String, Integer> extractData(ResultSet rs)
							throws SQLException {
						Map<String, Integer> resultMap = new HashMap<String, Integer>();

						while (rs.next()) {
							String appKey = rs.getString("msbo_app_key");
							int cnt = rs.getInt("msgCnt");
							resultMap.put(appKey, cnt);
						}
						return resultMap;
					}
				});
		return resultMap;
	}

	@Override
	public Map<? extends String, ? extends Map<String, CsChatSummary>> getCsStatisticDay() {
		StringBuilder select = new StringBuilder();
		select.append(" SELECT");
		select.append(" 	e.usac_app_key,");
		select.append(" 	e.usac_user_account,");
		select.append(" 	a.chme_chat_id finishChat,");
		select.append(" 	b.cAccount,");
		select.append(" 	c.chme_chat_id unfinishChat");
		select.append(" FROM");
		select.append(" 	hiauth.tb_user_account_r e");
		select.append(" LEFT OUTER JOIN(");
		select.append(" 	SELECT DISTINCT");
		select.append(" 		chme_app_key,");
		select.append(" 		chme_chat_id,");
		select.append(" 		chme_user_account sAccount");
		select.append(" 	FROM");
		select.append(" 		hiauth.tb_user_account_r");
		select.append(" 	LEFT OUTER JOIN hichat.tb_chat_user_r ON chme_user_account = usac_user_account");
		select.append(" 	WHERE");
		select.append(" 		usac_user_type IN(2, 3)");
		select.append(" 	AND chme_status = 1");
		select.append(" 	AND chme_type = 0");
		select.append(" 	AND DATE(chme_update_time)= DATE(now())");
		select.append(" )a ON a.sAccount = e.usac_user_account");
		select.append(" LEFT OUTER JOIN(");
		select.append(" 	SELECT DISTINCT");
		select.append(" 		chme_app_key,");
		select.append(" 		chme_chat_id,");
		select.append(" 		chme_user_account cAccount");
		select.append(" 	FROM");
		select.append(" 		hichat.tb_chat_user_r,");
		select.append(" 		hiauth.tb_user_account_r");
		select.append(" 	WHERE");
		select.append(" 		usac_user_type = 0");
		select.append(" 	AND chme_status = 1");
		select.append(" 	AND chme_type = 0");
		select.append(" 	AND DATE(chme_update_time)= DATE(now())");
		select.append(" 	AND chme_user_account = usac_user_account");
		select.append(" )b ON a.chme_chat_id = b.chme_chat_id");
		select.append(" LEFT OUTER JOIN(");
		select.append(" 	SELECT");
		select.append(" 		chme_user_account,");
		select.append(" 		chme_chat_id");
		select.append(" 	FROM");
		select.append(" 		hichat.tb_chat_user_r,");
		select.append(" 		hiauth.tb_user_account_r");
		select.append(" 	WHERE");
		select.append(" 		usac_user_type IN(2, 3)");
		select.append(" 	AND chme_status = 0");
		select.append(" 	AND chme_type = 0");
		select.append(" 	AND chme_user_account = usac_user_account");
		select.append(" )c ON e.usac_user_account = c.chme_user_account");
		select.append(" WHERE");
		select.append(" 	e.usac_user_type IN(2, 3)");
		Map<String, Map<String, CsChatSummary>> result = null;
		try {
			result = this
					.getJdbcTemplate()
					.query(select.toString(),
							new ResultSetExtractor<Map<String, Map<String, CsChatSummary>>>() {
								@Override
								public Map<String, Map<String, CsChatSummary>> extractData(
										ResultSet rs) throws SQLException {
									Map<String, Map<String, CsChatSummary>> resultMap = new HashMap<String, Map<String, CsChatSummary>>();
									while (rs.next()) {
										String appKey = rs
												.getString("usac_app_key");
										if (!resultMap.containsKey(appKey)) {
											Map<String, CsChatSummary> ccsMap = new HashMap<String, CsChatSummary>();
											resultMap.put(appKey, ccsMap);
										}
										String userAccount = rs
												.getString("usac_user_account");
										CsChatSummary ccs = resultMap.get(
												appKey).get(userAccount);
										if (ccs == null) {
											ccs = new CsChatSummary();
											ccs.setUserId(userAccount);
											resultMap.get(appKey).put(
													userAccount, ccs);
										}
										long chatId = rs
												.getLong("finishChat");
										if (chatId > 0) {
											ccs.getFinishChatSet().add(chatId);
										}
										String cAccount = rs
												.getString("cAccount");
										if (StringUtils.isNotEmpty(cAccount)) {
											ccs.getReceptionPersonSet().add(
													cAccount);
										}
										chatId = rs.getLong("unfinishChat");
										if(chatId>0){
											ccs.addUnfinishChat(chatId);
										}
									}
									return resultMap;
								}
							});

		} catch (Exception e) {

		}
		return result;
	}

}
