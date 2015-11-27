Netty Based Server Guide
=====================

# About
 It is a framework for rapid development of customized, high-performance application server for, based netty, integrated spring, log4j, etc.

## What is Netty?

Netty是一个异步的事件驱动网络应用框架，具有高性能、高扩展性等特性。  
Netty提供了统一的底层协议接口，使得开发者从底层的网络协议（比如TCP/IP、UDP）中解脱出来。  
就使用来说，开发者只要参考 Netty提供的若干例子和它的指南文档，
就可以放手开发基于Netty的服务端程序了。

[netty on github](https://github.com/netty/netty)


## 应用开发框架

### Pre-requisites
 * jdk1.6+
 * maven2+
 
### 代码结构
* com.paaple.framework.startup
 - Daemon
* com.paaple.framework.handler.WebSocketServerHandler
 - 处理HTTP请求、websocket连接的关键类
* com.paaple.framework.handler.ConsoleHandler
 - 后台管理处理器
 - 实现了服务状态查看/服务关闭功能
 - 待完善
 
### 服务的部署（linux环境下）和访问
* svn 获取项目源代码
* 执行mvn clean package打包项目到target目录下，名字类似 netty.based-0.1.0-r-[revision]-release.tar.gz 
* 调用tar -xf netty.based-X.X.X-release.tar.gz,解压文件
* 进入bin目录，调用./app-server.sh start 启动服务
* 访问：http://ip:8080/ - 业务访问 | http://ip:8086/ - 管理后台

### maven的应用
* 每核心分配1个线程来执行：mvn -T 1C cmd
* 应用maven-assembly-plugin加强了package功能，实现了保持脚本执行属性和tar.gz打包

### 项目功能

* 基于http的业务服务框架：包括业务服务和后台管理服务
* 基于maven的持续集成支持
* 基于脚本（sshpass needed）的部署

## 其他参考

* [User Guide for 4.x](http://netty.io/wiki/user-guide-for-4.x.html)

## 后续计划

* 加强ConsoleServer的功能
* spring4集成
* 单元测试功能添加
