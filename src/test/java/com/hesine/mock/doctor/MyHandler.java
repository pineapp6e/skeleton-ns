/**
 * 
 */
package com.hesine.mock.doctor;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.apache.log4j.Logger;

/**
 * @author pineapple
 *
 */
public class MyHandler extends ChannelDuplexHandler {
	
	private static Logger logger = Logger.getLogger(MyHandler.class);
	
	private int doctorIdx;
	
	public MyHandler(int doctorIdx) {
		super();
		this.doctorIdx = doctorIdx;
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				logger.info("READER_IDLE 读超时, disconnect doctor_"+doctorIdx);
				// Close
	            ctx.channel().writeAndFlush(new CloseWebSocketFrame());
				ctx.disconnect();
			}else if(e.state() == IdleState.WRITER_IDLE){
				logger.debug("WRITER_IDLE 写超时, doctor_"+doctorIdx+" send heart beat.");
				 // Ping
	            ctx.channel().writeAndFlush(new PingWebSocketFrame(Unpooled.copiedBuffer(new byte[]{1, 2, 3, 4, 5, 6})));

			}
		}
	}

}
