/**
 * 
 */
package com.hesine.hichat.access.handler;

import static com.hesine.hichat.access.common.EnumConstants.HTML_CONTENT_TYPE;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;

import com.hesine.hichat.access.bo.ExampleBO;
import com.hesine.util.DataAccessFactory;

/**
 * @author wanghua
 * 
 */
public class HelloWorldHandler extends SimpleChannelInboundHandler<Object> {
	
	private ExampleBO exampleBO = (ExampleBO)DataAccessFactory.dataHolder().get("exampleBO");
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof HttpRequest) {
			int one = exampleBO.getOne();
			HttpRequest req = (HttpRequest) msg;
			ResponseGenerator.writeResponse(ctx.channel(), req, "Hello World!"+one,
					HTML_CONTENT_TYPE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

}
