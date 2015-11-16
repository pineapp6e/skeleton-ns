/**
 * 
 */
package com.hesine.hichat.access.handler;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
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
import com.hesine.hichat.access.model.DispatchResult;

/**
 * @author pineapple Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger logger = Logger
			.getLogger(WebSocketServerHandler.class);

	public static final AttributeKey<String> CLIENT_KEY = new AttributeKey<String>("clientName");
	public static final AttributeKey<String> APP_KEY = new AttributeKey<String>("appKey");
	
	private static final String WEBSOCKET_PATH = "/websocket";

	private volatile int count = 0;
	
	private WebSocketServerHandshaker handshaker;

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
		if (!req.getDecoderResult().isSuccess()) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
					BAD_REQUEST));
			return;
		}

		// Allow only GET methods.
//		if (req.getMethod() == POST) {
//			logger.info("req content:"+req.content().toString());
//			DispatchResult<?> dispatchResult = DispatcherFactory.dispatcher(req);
//			if (dispatchResult != null && !dispatchResult.isInvalid()) {
//				ctx.pipeline().addLast("consultDoctor", dispatchResult.getNextHandler());
//				ctx.fireChannelRead(dispatchResult.getMessage());
//				return;
//			}
//			return;
//		}

		if (req.getMethod() == GET) {
			// Send the demo page and favicon.ico
			if ("/".equals(req.getUri())) {
				ByteBuf content = WebSocketServerIndexPage
						.getContent(getWebSocketLocation(req));
				FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1,
						OK, content);

				res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
				setContentLength(res, content.readableBytes());

				sendHttpResponse(ctx, req, res);
				return;
			}
			if ("/favicon.ico".equals(req.getUri())) {
				FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1,
						NOT_FOUND);
				sendHttpResponse(ctx, req, res);
				return;
			}
			if(count>0){
				sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1,
						FORBIDDEN));
				return;
			}
		}

		logger.info("req method:"+req.getMethod() + ",URI:"+req.getUri());
		logger.info("req content:"+req.content().toString());
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
		//ctx.channel().writeAndFlush(new TextWebSocketFrame("server socket open"));
		count++;
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

		// Send the uppercase string back.
		String request = ((TextWebSocketFrame) frame).text();
		logger.info(String.format("%s received %s", ctx.channel(), request));
		DispatchResult<?> dispatchResult = DispatcherFactory.dispatcher(request);
		if (dispatchResult != null && !dispatchResult.isInvalid()) {
			ctx.pipeline().addLast(dispatchResult.getNextHandler());
			ctx.fireChannelRead(dispatchResult.getMessage());
			return;
		}
		// ctx.channel().write(new TextWebSocketFrame(request.toUpperCase()));
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx,
			FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(),
					CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			setContentLength(res, res.content().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
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

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		String serviceId = ctx.channel().attr(CLIENT_KEY).get();
		String appKey = ctx.channel().attr(APP_KEY).get();
		
		if(serviceId == null || appKey == null){
			super.channelInactive(ctx);
		}else{
			logger.info("channel "+serviceId+" inactive, close channel.");
			ctx.close();
		}
	}
	
	
}
