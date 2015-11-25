/**
 * 
 */
package com.papple.ws.test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.websocket.ContainerProvider;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.jsr356.ClientContainer;

import com.codahale.metrics.ConsoleReporter;
import com.papple.ws.test.util.Common;

/**
 * @author pineapple
 *
 */
public class SimpleWebSocketClient {

	private static Logger logger = Logger.getLogger(SimpleWebSocketClient.class
			.getName());

	/**
	 * @param args
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws Exception {
		String localIP = "127.0.0.1";
		if (args.length >= 1) {
			localIP = args[0];
		}

		final ClientContainer container = (ClientContainer) ContainerProvider
				.getWebSocketContainer();
		// container.setDefaultMaxSessionIdleTimeout(10000);
		container.getClient()
				.setBindAdddress(new InetSocketAddress(localIP, 0));
		final int batchSize = Common.batchSize;
		final long setupInterval = Common.setupInterval;
		final int totalClinets = Common.totalClients;

		startReport();

		final ExecutorService threadPool = Executors
				.newFixedThreadPool(Common.conf.getInt("setup.threadpool.size"));

		new Thread(new Runnable() {
			@Override
			public void run() {
				long t = 0;
				int count = 0;
				while (count < totalClinets) {
					t = System.currentTimeMillis();
					for (int i = 0; i < batchSize && count < totalClinets; i++) {
						threadPool.submit(new Runnable() {
							@Override
							public void run() {
								try {
									container.connectToServer(
											new SimpleEchoSocket(), new URI(
													Common.serverIP));
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
						count++;
					}
					t = setupInterval + t - System.currentTimeMillis();
					logger.info("send " + count + " and sleep " + t + "ms");
					if (t > 0) {
						try {
							TimeUnit.MILLISECONDS.sleep(t);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

	public static void startReport() {
		ConsoleReporter reporter = ConsoleReporter.forRegistry(Common.metrics)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.SECONDS).build();
		reporter.start(10, TimeUnit.SECONDS);
	}

}
