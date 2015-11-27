/**
 * 
 */
package com.papple.framework.dao.impl;

import org.springframework.stereotype.Component;

import com.papple.framework.dao.ExampleDAO;


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
