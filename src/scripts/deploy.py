#!/usr/bin/env python
# -*- coding: utf-8 -*-

from fabric.api import local, cd, sudo
from fabric.api import put, get
from fabric.api import env, task, run


path = '/srv'

@task
def status():
    run("ps -ef |grep hichat.access-0.1.0-rnull.jar")
    run('screen -wipe', warn_only=True)

@task
def deploy():
	#get_hosts()
    put('E:/hichat/hichat/hichat-access/target/hichat.access-0.1.0-rnull.jar',path,use_sudo=True) #put jmeter to remote:~
    put('E:/hichat/hichat/hichat-model/target/hichat_model-0.0.1-SNAPSHOT.jar',path,use_sudo=True) #put jmeter to remote:~
    with cd(path):
    	run('rm -f /srv/hichat/hichat-access/lib/hichat.access-0.1.0-rnull.jar')
    	run('cp /srv/hichat.access-0.1.0-rnull.jar /srv/hichat/hichat-access/lib/')
    	run('rm -f /srv/hichat/hichat-access/lib/hichat_model-0.0.1-SNAPSHOT.jar')
    	run('cp /srv/hichat_model-0.0.1-SNAPSHOT.jar /srv/hichat/hichat-access/lib/')
    	stop()
        start()
        #sudo('tar xzf videome.serv-1-release.tar.gz')

@task
def uninstalled():
    with cd(path):
        print 'uninstalled begin'
        stop()
        #sudo('rm -rf videome.serv-1*')
        print 'uninstalled end'

@task
def stop():
	#run('/srv/hichat/hichat-access/bin/nmsg-server.sh stop')
    kill_process(str="hichat.access")

@task
def start():
    #sudo('screen -S videome.serv -d -m videome.serv-1/bin/netty.sh run; sleep 1')
    #sudo('screen -ls |grep videome.serv')
    run('/srv/hichat/hichat-access/bin/nmsg-server.sh start');
    run('screen -S hichat-access -d -m /srv/hichat/hichat-access/bin/nmsg-server.sh run; sleep 1 ')

@task
def get_hosts():
    env.user='root'
    env.password = '111111'        
    env.hosts = ['172.27.244.62', ]       

@task
def kill_process(str):
    run("ps ax |grep %s |grep -v grep | awk '{print $1}' | xargs kill -9" % str, warn_only=True)