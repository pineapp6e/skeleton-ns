/**
 * 
 */
package com.hesine.hichat.access.bo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hesine.hichat.access.bo.ExampleBO;
import com.hesine.hichat.access.dao.ExampleDAO;

/**
 * @author wanghua
 *
 */
@Component("exampleBO")
public class ExampleOneBO implements ExampleBO {
	
	@Autowired
	private ExampleDAO exampleDAO;

	@Override
	public int getOne(){
		return exampleDAO.getOne();
	}
}
