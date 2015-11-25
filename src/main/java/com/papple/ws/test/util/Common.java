/**
 * 
 */
package com.papple.ws.test.util;

import java.util.UUID;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * @author pineapple
 *
 */
public class Common {
	public static String name = UUID.randomUUID().toString();
	public static Config conf = ConfigFactory.load();

	public static String serverIP = conf.getString("server.uri");
	public static int batchSize = conf.getInt("setup.batchSize");
	public static long setupInterval = conf.getLong("setup.interval");
	public static int totalClients = conf.getInt("total.clients");
	public static int totalSize = conf.getInt("total.size");
	public static long delay = conf.getLong("thread.delay");
	public static long interval = conf.getLong("thread.interval");

	public static MetricRegistry metrics = new MetricRegistry();
	public static Meter setupRate = metrics.meter("Setup Rate for " + name);
	public static Meter messageRate = metrics.meter("Message Rate for " + name);
	public static Counter activeWebSockets = metrics
			.counter("Active WebSockets for  " + name);
	public static Counter websocketError = metrics
			.counter("WebSocket Errors for  " + name);
	public static Histogram messageLatency = metrics
			.histogram("Message latency for  " + name);
}
