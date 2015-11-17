/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
//The MIT License
//
//Copyright (c) 2009 Carl Bystršm
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package com.hesine.mock.doctor;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.hesine.hichat.access.handler.WebSocketServerHandler;

public class WebSocketClient {
	private static final Logger logger = Logger.getLogger(WebSocketClient.class);
    private final URI uri;
    private int clientIndx;
    
    public WebSocketClient(URI uri, int clientIndx) {
        this.uri = uri;
        this.clientIndx = clientIndx;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            String protocol = uri.getScheme();
            if (!"ws".equals(protocol)) {
                throw new IllegalArgumentException("Unsupported protocol: " + protocol);
            }

            HttpHeaders customHeaders = new DefaultHttpHeaders();
            customHeaders.add("MyHeader", "MyValue_"+clientIndx);

            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            final WebSocketClientHandler handler =
                    new WebSocketClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, false, customHeaders));

            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline pipeline = ch.pipeline();
                     pipeline.addLast("http-codec", new HttpClientCodec());
                     pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
                     pipeline.addLast("ws-handler", handler);
                 }
             });

            logger.info("WebSocket Client connecting");
            Channel channel = b.connect(uri.getHost(), uri.getPort()).sync().channel();
            
            channel.attr(WebSocketServerHandler.CLIENT_KEY).set("client_"+clientIndx);
            handler.handshakeFuture().sync();
            
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
    	int port = 8080;
		String ip = "localhost";
		int doctorCnt = 1000;
		int offset = 0;
		if (args.length == 0) {
			logger.info(" 使用缺省设置: localhost 8080 100(模拟客户端个数)  0(客户端编号偏移量)");
		} else if (args.length == 4) {
			ip = args[0];
			port = NumberUtils.toInt(args[1]);
			doctorCnt = NumberUtils.toInt(args[2]);
			offset = NumberUtils.toInt(args[3]);
		}else{
			logger.info("用法:");
			logger.info("java -jar 类名 ip 端口  医生端个数(必须为整数)  医生起始编号偏移量(必须为整数)");
			System.exit(0);
		}
		
        URI uri = new URI("ws://"+ip+":"+port+"/websocket");
        for (int i = offset; i < doctorCnt+offset; i++) {
        	final int doctorIndx = i+1; 
			final WebSocketClient client = new WebSocketClient(uri, doctorIndx);
			new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						client.run();
					}catch (Exception e) {
						e.printStackTrace();
					}					
					logger.info("client_"+doctorIndx+" onine success");
				}
			}).start();
			TimeUnit.MILLISECONDS.sleep(100);
		}
    }
}
