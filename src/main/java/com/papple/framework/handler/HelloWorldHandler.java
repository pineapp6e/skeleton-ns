/**
 * 
 */
package com.papple.framework.handler;

import static com.papple.framework.common.EnumConstants.HTML_CONTENT_TYPE;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;

import com.papple.framework.bo.ExampleBO;
import com.papple.framework.util.DataAccessFactory;

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
