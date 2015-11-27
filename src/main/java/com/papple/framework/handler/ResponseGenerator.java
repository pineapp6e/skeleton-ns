/**
 * 
 */
package com.papple.framework.handler;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author wanghua
 * 
 */
public class ResponseGenerator {

	private static Logger logger = Logger.getLogger(ResponseGenerator.class);

	public static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }       
        res.headers().set("Access-Control-Allow-Origin","*");            
        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        f.addListener(ChannelFutureListener.CLOSE);
    }
	
	public static void writeResponse(Channel channel, HttpRequest request,
			String content, String contentType) {
		// Decide whether to close the connection or not.
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request
				.headers().get(CONNECTION))
				|| request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
				&& !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request
						.headers().get(CONNECTION));

		ByteBuf buf = StringUtils.isEmpty(content) ? EMPTY_BUFFER
				: wrappedBuffer(content.getBytes(CharsetUtil.UTF_8));
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		response.headers().set(CONTENT_TYPE, contentType);

		if (!close) {
			// There's no need to add 'Content-Length' header
			// if this is the last response.
			response.headers().set(CONTENT_LENGTH, buf.readableBytes());
		}
		logger.info("Response content:" + content);
		// Write the response.
		ChannelFuture future = channel.writeAndFlush(response);
		// Close the connection after the write operation is done if necessary.
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	public static void WriteErrorResponse(Channel channel, HttpRequest request,
			Throwable cause) {
		/*if (cause instanceof JSONException) {
			writeResponse(channel, request, INCORRECT_JSON_EXCEPTION, PLAIN_CONTENT_TYPE);
		} else if (cause instanceof CannotGetJdbcConnectionException) {
			writeResponse(channel, request, SERVICE_UNAVAILABLE_EXCEPTION, PLAIN_CONTENT_TYPE);
		} else if (cause instanceof EmptyResultDataAccessException) {
			writeResponse(channel, request, INVALID_DATA_SET_EXCEPTION, PLAIN_CONTENT_TYPE);
		} else if (cause instanceof ExceedQueueLimitException) {
			writeResponse(channel, request, EXCEED_LIMIT_SIZE_EXCEPTION, PLAIN_CONTENT_TYPE);
		} else if (cause instanceof InvalidPnURLException) {
			writeResponse(channel, request, INVALID_PN_URL_EXCEPTION, PLAIN_CONTENT_TYPE);
		} else {
			writeResponse(channel, request, UNKNOWN_EXCEPTION, PLAIN_CONTENT_TYPE);
		}*/
	}

}
