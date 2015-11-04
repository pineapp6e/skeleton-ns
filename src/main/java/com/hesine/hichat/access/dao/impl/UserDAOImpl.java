package com.hesine.hichat.access.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.dao.UserDAO;
import com.hesine.hichat.model.AccountInfo;

@Component("userDAO")
public class UserDAOImpl extends BaseDAO implements UserDAO {

	@Override
	public AccountInfo getAccountInfo(String appKey, String userAccount) {
		StringBuilder select = new StringBuilder();
		select.append("select * from hiauth.tb_user_account_r");
		select.append(" where usac_app_key=? and usac_user_account=?");
		AccountInfo accountInfo = null;
		try {
			accountInfo = this.getJdbcTemplate().queryForObject(
					select.toString(), new RowMapper<AccountInfo>() {
						@Override
						public AccountInfo mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							AccountInfo accountInfo = new AccountInfo();
							accountInfo.setUserId(rs
									.getString("usac_user_account"));
							accountInfo.setNickName(rs
									.getString("usac_nickname"));
							accountInfo.setAppKey(rs.getString("usac_app_key"));
							accountInfo.setUserType(rs.getInt("usac_user_type"));
							return accountInfo;
						}
					}, appKey, userAccount);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		return accountInfo;
	}

	@Override
	public int updateState(String appKey, String userAccount,
			String deviceToken, int state) {
		String sql = "update hiauth.tb_auth_account_r "
				+ " set auac_user_state = :auac_user_state "
				+ " where auac_user_account = :auac_user_account "
				+ " and auac_app_key = :auac_app_key and auac_device_token = :auac_device_token";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("auac_user_state", state);
		paramMap.put("auac_user_account", userAccount);
		paramMap.put("auac_app_key", appKey);
		paramMap.put("auac_device_token", deviceToken);
		return this.getNamedParameterJdbcTemplate().update(sql, paramMap);
	}
}
