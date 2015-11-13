package com.hesine.hichat.access.service;

import org.apache.log4j.Logger;

import com.hesine.hichat.access.service.snapshot.SnapshotService;
import com.hesine.util.DataAccessFactory;

/**
 * Application Container
 * 
 * Initialise Application by Configuration
 * 
 * @author liyan
 * 
 */
public class ApplicationConfig {

	private static Logger log = Logger.getLogger(ApplicationConfig.class
			.getName());
	private static SnapshotService snapshot = null;

	/**
	 * @return the snapshot
	 */
	public static SnapshotService getSnapshotService() {
		return snapshot;
	}

	/**
	 * Initialise Application by Configuration
	 */
	public static void init() {

		snapshot = new SnapshotService();
		log.info("App init...");
		DataAccessFactory.initDataAccessMysqlByXML();
	}


	/**
	 * call before close service
	 */
	public static void release() {
	}
}
