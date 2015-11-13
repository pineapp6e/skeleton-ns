package com.hesine.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hesine.hichat.access.bo.ExampleBO;



public class DataAccessFactory {
	
	private static Logger log = Logger.getLogger(DataAccessFactory.class);

	private static  ApplicationContext mysqlCtxXml = null;
	private static Map<String,Object> dataHolder = new HashMap<String,Object>();
	
	public static void initDataAccessMysqlByXML(){
		
			log.info("init Data Access Objects[Mysql] start...");
			mysqlCtxXml=new ClassPathXmlApplicationContext("spring-config.xml"); 
			dataHolder.put("exampleBO", (ExampleBO)mysqlCtxXml.getBean("exampleBO"));
			//TODO
			log.info("init Data Access Objects[Mysql] over.");
	}
	
	
	public static ApplicationContext getMysqlCtxXml() {
		return mysqlCtxXml;
	}


	
	public static Map<String, Object> dataHolder() {
		return dataHolder;
	}


	public static void main(String[] args) {
		DataAccessFactory.initDataAccessMysqlByXML();
//		MessageDAO messageDAO = (MessageDAO)DataAccessFactory.dataHolder.get("messageDAO");
//		List<String> removeIds = new ArrayList<String>();
//		removeIds.add("4600013860217351375867848705");
//		removeIds.add("4600013860217351375868558519");
//		removeIds.add("4600013860217351375869181472");
//		removeIds.add("4600013860217351375869191717");
//		messageDAO.removeQueue(removeIds,"","");
	}
	
}
