/**
 * 
 */
package com.hesine.mock.client;

import java.util.Date;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.hesine.hichat.access.util.Common;

/**
 * @author pineapple
 *
 */
/**
 * Basic Echo Client Socket
 */
@ClientEndpoint
public class SimpleEchoSocket {

	private static Logger logger = Logger.getLogger(SimpleEchoSocket.class
			.getName());

	@OnMessage
	public void onMessage(Session session, String message) {
		Common.messageRate.mark();
		long timestamp = Long.valueOf(message);
		Common.messageLatency.update(System.currentTimeMillis() - timestamp);
		logger.info("received " + new Date(timestamp));
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		Common.activeWebSockets.inc();
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		Common.activeWebSockets.dec();
		logger.info("closed because of " + reason.getReasonPhrase());
	}

	@OnError
	public void onError(Session session, Throwable t) {
		Common.websocketError.inc();
		logger.error(t.getMessage());
	}
}