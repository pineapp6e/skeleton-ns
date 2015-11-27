/**
 * 
 */
package com.papple.framework.bo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.papple.framework.bo.ExampleBO;
import com.papple.framework.dao.ExampleDAO;

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
