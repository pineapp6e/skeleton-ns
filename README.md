Netty Based Server- Websocket C1000K-Server branch Guide
=====================

# About
  This is a java C1000k-Servers implementation.

### Pre-requisites
 * jdk1.6+
 * maven2+
 
### 服务的部署（linux环境下）和访问
* git clone 
* 执行mvn clean package打包项目到target目录下，名字类似 ws.server.demo-0.1.0-r-[revision]-release.tar.gz 
* 调用tar -xf ws.server.demo-0.1.0-r-X.X.X-release.tar.gz,解压文件
* 进入bin目录，调用./ws-server.sh start 启动服务,日志见 log目录下



