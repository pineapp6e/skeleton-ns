/**
 * 
 */
package com.hesine.hichat.access.startup;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import org.apache.log4j.Logger;

import com.hesine.hichat.access.handler.ConsoleHandler;
import com.hesine.hichat.access.handler.WebSocketServerInitializer;
import com.hesine.hichat.access.service.ApplicationConfig;

/**
 * @author liyan
 * 
 */
public class HttpServer implements Server {

	private static Logger log = Logger.getLogger(HttpServer.class.getName());

	private static int businessPort, consolePort;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.li3huo.netty.Server#init()
	 */
	public void init() {

		String envPort = System.getenv("PORT_BUSINESS");
		if (null != envPort) {
			businessPort = Integer.parseInt(envPort);
		}
		if (businessPort <= 0) {
			businessPort = 8080;
		}
		consolePort = 8085;

		ApplicationConfig.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.li3huo.netty.Server#start()
	 */
	public void start() throws Exception {
		startBusiness();
		startConsole();
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
			System.out.println("Web socket server started at port "
					+ businessPort + '.');
			System.out
					.println("Open your browser and navigate to http://localhost:"
							+ businessPort + '/');
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}

	}

	private void startConsole() throws Exception {

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
							@Override
							public void initChannel(SocketChannel ch)
									throws Exception {
								ChannelPipeline pipeline = ch.pipeline();
								pipeline.addLast(new HttpResponseEncoder());
								pipeline.addLast(new HttpRequestDecoder());
								pipeline.addLast(new ConsoleHandler());
							}
						}).option(ChannelOption.SO_REUSEADDR, true);
		b.bind(consolePort).sync();
		log.info("Console start at " + consolePort);
	}

	public void stop() {

	}

	public void status() {
		log.info("Show Status");
	}
}
