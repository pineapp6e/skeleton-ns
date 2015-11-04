package com.hesine.hichat.access.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.dao.NotifyDAO;
import com.hesine.hichat.access.model.NotifyRequest;

@Component("notifyDAO")
public class NotifyDAOImpl extends BaseDAO implements NotifyDAO {

    @Override
    public int insertMessageBox(String msgId, String account, NotifyRequest notifyRequest) {
        String insertSql = "insert into hichat.tb_msg_box_r"
                + "(msbo_id, msbo_user_account, msbo_content, msbo_type, msbo_subtype, msbo_create_time) "
                + "values (:msbo_id, :msbo_user_account, :msbo_content, :msbo_type, :msbo_subtype, :msbo_create_time)";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("msbo_id", msgId);
        paramMap.put("msbo_user_account", account);
        paramMap.put("msbo_content", notifyRequest.getMessage());
        paramMap.put("msbo_type", 1);
        paramMap.put("msbo_subtype", notifyRequest.getNotifyType());
        paramMap.put("msbo_create_time", new Timestamp(System.currentTimeMillis()));
        
        return this.getNamedParameterJdbcTemplate().update(insertSql, paramMap);
    }

    @Override
    public List<String> getDoctorIds(String hospitalIds) {
        String selectSql = "select usex_user_account from hi_auth.tb_user_extend_r where usex_group_id in (" + hospitalIds + ")";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        List<String> list = this.getNamedParameterJdbcTemplate().query(selectSql, paramMap, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                String account = rs.getNString("usex_user_account");
                return account;
            }
            
        });
        return list;
    }

    @Override
    public int insertMessageQueue(String account, String msgId) {
        String insertSql = "insert into hichat.tb_msg_queue_r"
                + "(msqu_user_account, msqu_msg_id, msqu_type, msqu_create_time) "
                + "values (:msqu_user_account, :msqu_msg_id, :msqu_type, :msqu_create_time)";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("msqu_user_account", account);
        paramMap.put("msqu_msg_id", msgId);
        paramMap.put("msqu_type", 1);
        paramMap.put("msqu_create_time", new Timestamp(System.currentTimeMillis()));
        return this.getNamedParameterJdbcTemplate().update(insertSql, paramMap);
    }

	@Override
	public int getQueueCnt(String account) {
		String select = "select count(*) from hichat.tb_msg_queue_r where msqu_user_account = :account";
		Map<String,String> paramMap = new HashMap<String,String>();
		paramMap.put("account", account);
		return this.getNamedParameterJdbcTemplate().queryForInt(select, paramMap);
	}

}
