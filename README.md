Websocket C1000K-Client branch Guide
=====================

# About
  This branch is a java implements client of [C1000k-Servers](https://github.com/smallnest/C1000K-Servers). server see brache ws-server.
  
For support more than 1,000,000 connections. Modify configure /etc/sysctl.conf

fs.file-max = 2000000 
fs.nr_open = 2000000  
net.ipv4.ip_local_port_range = 1024 65535

Each client can create 60000 connections (<65535) on each IP so you should create multiple IP addresses on one Test Server for clients. I have created 10 virtual internal IP addresses on each client.

Servers will send one message per minutes to all 1,200,000 connections. Each message only contains current time of this server. Clients can output metrics to monitor. 

Run mvn clean package 

### Pre-requisites
 * jdk1.6+
 * maven2+
 
### 服务的部署（linux环境下）和访问
* git clone 
* 执行mvn clean package打包项目到target目录下，名字类似 ws.client.demo-0.1.0-r-[revision]-release.tar.gz 
* 调用tar -xf ws.client.demo-0.1.0-r-X.X.X-release.tar.gz,解压文件
* 进入bin目录，调用./ws-client.sh [bindLocalIP] start 启动服务, 参数 bindLocalIP 为启动时绑定的本机IP，如配置
了多个虚拟IP， 则可绑定多个以执行，日志见 log目录下



