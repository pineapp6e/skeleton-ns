Netty Server Guide
=====================
转载请注明：**Powered by li3huo.com**  
技术交流请关注新浪微博 @li3huo

# Netty Server Framework

## What is Netty?

Netty是一个异步的事件驱动网络应用框架，具有高性能、高扩展性等特性。  
Netty提供了统一的底层协议接口，使得开发者从底层的网络协议（比如TCP/IP、UDP）中解脱出来。  
就使用来说，开发者只要参考 Netty提供的若干例子和它的指南文档，
就可以放手开发基于Netty的服务端程序了。

Netty提供的功能包括：  
1.优秀的核心设计：
 * 抛弃jdk自实现的buffer，减少复制所带来的消耗；
 * 庞大而清晰的channel体系，灵活支持NIO/OIO及TCP/UDP多维组合；
 * 基于事件的过程流转以及完整的网络事件响应与扩展；

2.丰富的协议支持：
 * HTTP/WebSocket
 * SSL/TLS
 * Google Protobuf
 * zlib/gzip
 * Large File Transfer
 * RTSP
 * Thrift[注1]

3.丰富的example，帮助开发者快速上手和理解

<img src#"https://netty.io/download/Main/WebHome/architecture.png" >

[netty on github](https://github.com/netty/netty)


## 应用开发框架

### 代码结构
* com.hesine.mim.startup
 - Daemon
* com.hesine.mim.service.handler.HttpMessageContext
 - 服务上下文
 - 目前封装了NioServerSocketChannelFactory对象和服务计数器
* com.hesine.mim.service.handler.HttpRequestHandler
 - 基于HTTP请求的处理器，框架的处理器基类
* com.hesine.mim.service.handler.ConsoleHandler
 - 后台管理处理器
 - 实现了服务状态查看/服务关闭功能

### 服务的部署（linux环境下）和访问
* svn 获取项目源代码
* 执行mvn clean package打包项目到target目录下，名字类似 mim-server-0.1.0-release.tar.gz
* 调用tar -xf mim-server-X.X.X-release.tar.gz,解压文件
* 进入bin目录，调用./mim-server.sh start 启动服务
* 访问：http://ip:8081/ - 业务访问 | http://ip:8086/ - 管理后台

### maven的应用
* 每核心分配1个线程来执行：mvn -T 1C cmd
* 应用maven-assembly-plugin加强了package功能，实现了保持脚本执行属性和tar.gz打包

### 项目功能

* 基于http的业务服务框架：包括业务服务和后台管理服务
* 基于maven的持续集成支持
* 基于脚本（sshpass needed）的部署

## 其他参考

* [The Netty Project 3.x User Guide](http://static.netty.io/3.6/guide/)
* [注1](https://github.com/facebook/nifty/): Nifty is an implementation of Thrift clients and servers on Netty, written by facebook

## 后续计划

* 继续加强ConsoleServer的功能：结合JMX/提供业务调用计数接口
* Known Issue:
 1. ab on mac don't work for this server. but also don't work for tomcat,   
so it must be issue of ab on mac.
