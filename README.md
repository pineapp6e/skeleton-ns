Skeleton-ns Guide
=====================

# About
 It is a framework for rapid development of customized, high-performance application server for, based netty, integrated spring, log4j, etc.

# Detail

## Pre-requisites
 * jdk1.6+
 * maven2+
 
## code structure
* com.paaple.framework.startup
 - Daemon
* com.paaple.framework.handler.WebSocketServerHandler
 - Handle http, websocket core class
* com.paaple.framework.handler.ConsoleHandler
 - Be perfect
 
## Build & Access
* git clone https://github.com/pineapp6e/skeleton-ns.git
* `cd skeleton-ns`
* `mvn clean package` package to target directory，looks like skeleton-ns-0.1.0-r-[revision]-release.tar.gz 
* `tar -xf skeleton-ns-0.1.0-r-X.X.X-release.tar.gz`
* `cd bin` and execute `./app-server.sh start` start the service
* access：http://ip:8080/ - Business access 

## Project Summary

* Base http/websocket's business framework
* Base maven support
* Base script（sshpass needed）deply

## Others

* [User Guide for 4.x](http://netty.io/wiki/user-guide-for-4.x.html)

## Next Steps

* enhance ConsoleHandler feature
* spring4 Integration
* add junit case
