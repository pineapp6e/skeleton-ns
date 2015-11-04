package com.hesine.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hesine.hichat.access.bo.ChatBO;
import com.hesine.hichat.access.bo.ChatOperationBO;
import com.hesine.hichat.access.bo.ChatStatusBO;
import com.hesine.hichat.access.bo.ExampleBO;
import com.hesine.hichat.access.bo.NotifyBO;
import com.hesine.hichat.access.dao.ChatDAO;
import com.hesine.hichat.access.dao.ChatOperationBAO;
import com.hesine.hichat.access.dao.UserDAO;



public class DataAccessFactory {
	
	private static Logger log = Logger.getLogger(DataAccessFactory.class);

	private static  ApplicationContext mysqlCtxXml = null;
	private static Map<String,Object> dataHolder = new HashMap<String,Object>();
	
	public static void initDataAccessMysqlByXML(){
		
			log.info("init Data Access Objects[Mysql] start...");
			mysqlCtxXml=new ClassPathXmlApplicationContext("spring-config.xml"); 
			dataHolder.put("exampleBO", (ExampleBO)mysqlCtxXml.getBean("exampleBO"));
			dataHolder.put("notifyBO", (NotifyBO)mysqlCtxXml.getBean("notifyBO"));
			dataHolder.put("chatOperationBO", (ChatOperationBO)mysqlCtxXml.getBean("chatOperationBO"));
			dataHolder.put("chatBO", (ChatBO)mysqlCtxXml.getBean("chatBO"));
			dataHolder.put("chatStatusBO", (ChatStatusBO)mysqlCtxXml.getBean("chatStatusBO"));
			dataHolder.put("userDAO", (UserDAO)mysqlCtxXml.getBean("userDAO"));
			dataHolder.put("chatOperationDAO", (ChatOperationBAO)mysqlCtxXml.getBean("chatOperationBAO"));
			dataHolder.put("chatDAO", (ChatDAO)mysqlCtxXml.getBean("chatDAO"));
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
