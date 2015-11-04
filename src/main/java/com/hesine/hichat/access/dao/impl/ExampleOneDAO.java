/**
 * 
 */
package com.hesine.hichat.access.dao.impl;

import org.springframework.stereotype.Component;

import com.hesine.hichat.access.dao.ExampleDAO;


/**
 * @author wanghua
 *
 */
@Component("exampleDAO")
public class ExampleOneDAO extends BaseDAO implements ExampleDAO {
	
	//private static final String TABLE_NAME = "";
	
	@Override
	public int getOne() {
		String sql = "select 1 ";
		return this.getJdbcTemplate().queryForInt(sql);
	}

	
}
