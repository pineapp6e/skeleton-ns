/**
 * 
 */
package com.hesine.hichat.access.handler;


import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import org.apache.log4j.Logger;

import com.hesine.hichat.access.model.ClientChannelMap;

/**
 * @author pineapple Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger logger = Logger
			.getLogger(WebSocketServerHandler.class);

	public static final AttributeKey<String> CLIENT_KEY = new AttributeKey<String>("clientName");
	public static final AttributeKey<String> APP_KEY = new AttributeKey<String>("appKey");
	
	private static final String WEBSOCKET_PATH = "/websocket";

	private WebSocketServerHandshaker handshaker;

//	private volatile int count = 0;
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	private void handleHttpRequest(ChannelHandlerContext ctx,
			FullHttpRequest req) throws Exception {
		// Handle a bad request.
//		if (!req.getDecoderResult().isSuccess()) {
//			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
//					BAD_REQUEST));
//			return;
//		}
//		
//		if (req.getMethod() == GET) {
//			// Send the demo page and favicon.ico
//			if ("/".equals(req.getUri())) {
//				ByteBuf content = WebSocketServerIndexPage
//						.getContent(getWebSocketLocation(req));
//				FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1,
//						OK, content);
//
//				res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
//				setContentLength(res, content.readableBytes());
//
//				sendHttpResponse(ctx, req, res);
//				return;
//			}
//			if ("/favicon.ico".equals(req.getUri())) {
//				FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1,
//						NOT_FOUND);
//				sendHttpResponse(ctx, req, res);
//				return;
//			}
//			if(count>0){
//				sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
//						FORBIDDEN));
//				return;
//			}
//			count++;
//		}

		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				getWebSocketLocation(req), null, false);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory
					.sendUnsupportedWebSocketVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), req);
			ClientChannelMap.add(ClientChannelMap.DEFAULT_GROUP, ctx.channel());
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx,
			WebSocketFrame frame) {

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(),
					(CloseWebSocketFrame) frame.retain());
			return;
		}
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(
					new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format(
					"%s frame types not supported", frame.getClass().getName()));
		}
		logger.info(frame.content().toString(CharsetUtil.UTF_8));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error(cause.getMessage(), cause);
		ctx.close();
	}

	private static String getWebSocketLocation(FullHttpRequest req) {
		return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
	}

}
