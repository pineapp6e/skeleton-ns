/**
 * 
 */
package com.hesine.hichat.access.startup;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hesine.hichat.access.handler.WebSocketServerInitializer;
import com.hesine.hichat.access.model.ClientChannelMap;
import com.hesine.hichat.access.util.Common;
import com.hesine.hichat.access.util.NotifyClientUtil;
/**
 * @author liyan
 * 
 */
public class HttpServer implements Server {

	private static Logger log = Logger.getLogger(HttpServer.class.getName());

	private static int businessPort;

	public void init() {

		String envPort = System.getenv("PORT_BUSINESS");
		if (null != envPort) {
			businessPort = Integer.parseInt(envPort);
		}
		if (businessPort <= 0) {
			businessPort = 8080;
		}
	}

	public void start() throws Exception {
		startBusiness();
	}

	private void startBusiness() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).childOption(ChannelOption.TCP_NODELAY, true)
					.channel(NioServerSocketChannel.class)
					.childHandler(new WebSocketServerInitializer());

			Channel ch = b.bind(businessPort).sync().channel();
			log.info("Web socket server started at port "
					+ businessPort + '.');
			log.info("Open your browser and navigate to http://localhost:"
							+ businessPort + '/');
			if(true){
				Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
			          String flag = UUID.randomUUID().toString();
			          if (ClientChannelMap.clientCnt() < Common.totalSize) {
			        	  log.info("current channels "+ ClientChannelMap.clientCnt()  +" for " + flag);
			          } else {
			            log.info("send msg to channels for " + flag);
			            NotifyClientUtil.notifyGroup(String.valueOf(System.currentTimeMillis()), ClientChannelMap.DEFAULT_GROUP);
			            log.info("sent msg to channels for "+flag+". current channels: "+ClientChannelMap.clientCnt());
			          }
			        }
			      }, Common.delay, Common.interval, TimeUnit.MILLISECONDS);
			}
			
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}

	}

	public void stop() {

	}

	public void status() {
		log.info("Show Status");
	}
}
