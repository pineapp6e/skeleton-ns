/**
 * Create at Jan 31, 2013
 */
package com.papple.ws.test.startup;

import org.apache.log4j.Logger;

/**
 * @author liyan
 * 
 *         Server bootstrap
 */
public class Bootstrap {

	private static Logger log = Logger.getLogger(Bootstrap.class.getName());
	
	/**
	 * Daemon object used by main.
	 */
	private static Server daemon = null;

	private static Server getInstance() {
		return new HttpServer();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (daemon == null) {
			daemon = getInstance();
			daemon.init();
		}
		String command = "start";
		if (args.length > 0) {
			command = args[args.length - 1];
		}
		if (command.equals("start")) {
			daemon.start();
			log.info("start success.");
		} else if (command.equals("stop")) {
			log.info("Stopping...");
			daemon.stop();
		} else if (command.equals("status")) {
			log.info("Status...");
			daemon.status();
		} else {
			log.warn("Bootstrap: command \"" + command + "\" does not exist.");
		}
	}

}
