#!/bin/sh

# -----------------------------------------------------------------------------
# Start/Stop Script for the NMSG Server
#
# Environment Variable Prequisites
#
#   NMSG_HOME   May point at your nmsg server "build" directory.
#
#   NMSG_BASE   (Optional) Base directory for resolving dynamic portions
#                   of a nmsg installation.  If not present, resolves to
#                   the same directory that NMSG_HOME points to.
#
#   NMSG_OPTS   (Optional) Java runtime options used when the "start",
#                   or "run" command is executed.
#
#   NMSG_TMPDIR (Optional) Directory path location of temporary directory
#                   the JVM should use (java.io.tmpdir).  Defaults to
#                   $NMSG_BASE/temp.
#
#
#   JAVA_OPTS       (Optional) Java runtime options used when the "start",
#                   "stop", or "run" command is executed.
#
#   JPDA_TRANSPORT  (Optional) JPDA transport used when the "jpda start"
#                   command is executed. The default is "dt_socket".
#
#   JPDA_ADDRESS    (Optional) Java runtime options used when the "jpda start"
#                   command is executed. The default is 8000.
#
#   JPDA_SUSPEND    (Optional) Java runtime options used when the "jpda start"
#                   command is executed. Specifies whether JVM should suspend
#                   execution immediately after startup. Default is "n".
#
#   JPDA_OPTS       (Optional) Java runtime options used when the "jpda start"
#                   command is executed. If used, JPDA_TRANSPORT, JPDA_ADDRESS,
#                   and JPDA_SUSPEND are ignored. Thus, all required jpda
#                   options MUST be specified. The default is:
#
#                   -Xdebug -Xrunjdwp:transport=$JPDA_TRANSPORT,
#                       address=$JPDA_ADDRESS,server=y,suspend=$JPDA_SUSPEND
#
#
#   NMSG_PID    (Optional) Path of the file which should contains the pid
#                   of nmsg startup java process, when start (fork) is used
#
# $Id: nmsg-server.sh 2013-08-28 17:24:19Z jim $
# -----------------------------------------------------------------------------
LC_ALL=en_US.UTF-8
# Business Server Port
export PORT_BUSINESS=8080

#Server Class for NMSG
_ServerClass=com.papple.framework.startup.Bootstrap

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
os400=false
darwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
Darwin*) darwin=true;;
esac

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set NMSG_HOME if not already set
[ -z "$NMSG_HOME" ] && NMSG_HOME=`cd "$PRGDIR/.." ; pwd`


# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
  # Bugzilla 37284 (reviewed).
  if $darwin; then
    if [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
      export JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home"
    fi
  else
    JAVA_PATH=`which java 2>/dev/null`
    if [ "x$JAVA_PATH" != "x" ]; then
      JAVA_PATH=`dirname $JAVA_PATH 2>/dev/null`
      JRE_HOME=`dirname $JAVA_PATH 2>/dev/null`
    fi
    if [ "x$JRE_HOME" = "x" ]; then
      # XXX: Should we try other locations?
      if [ -x /usr/bin/java ]; then
        JRE_HOME=/usr
      fi
    fi
  fi
  if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
    echo "Neither the JAVA_HOME nor the JRE_HOME environment variable is defined"
    echo "At least one of these environment variable is needed to run this program"
    exit 1
  fi
fi
if [ -z "$JAVA_HOME" -a "$1" = "debug" ]; then
  echo "JAVA_HOME should point to a JDK in order to run in debug mode."
  exit 1
fi
if [ -z "$JRE_HOME" ]; then
  JRE_HOME="$JAVA_HOME"
fi

# Set standard commands for invoking Java.
  _RUNJAVA="$JRE_HOME"/bin/java

if [ -z "$NMSG_BASE" ] ; then
  NMSG_BASE="$NMSG_HOME"
fi

NMSG_PID="$NMSG_BASE"/pid

if [ -z "$NMSG_TMPDIR" ] ; then
  # Define the java.io.tmpdir to use for nmsg
  NMSG_TMPDIR="$NMSG_BASE"/temp
fi

# Bugzilla 37848: When no TTY is available, don't output to console
have_tty=0
if [ "`tty`" != "not a tty" ]; then
    have_tty=1
fi

# ----- Execute The Requested Command -----------------------------------------

# Bugzilla 37848: only output this if we have a TTY
if [ $have_tty -eq 1 ]; then
  echo "Using NMSG_BASE:   $NMSG_BASE"
  echo "Using NMSG_HOME:   $NMSG_HOME"
  echo "Using NMSG_TMPDIR: $NMSG_TMPDIR"
  echo "Using JAVA_HOME:       $JAVA_HOME"
fi

# Add dependency library to CLASSPATH
if [ -r "$NMSG_HOME"/lib ]; then
	for x in "$NMSG_HOME"/lib/*
	do
	    CLASSPATH="$CLASSPATH":$x 
	done
else
	echo "debug mode. loading lib from project"
	if [ -r "$NMSG_HOME"/../lib ]; then
		cd "$NMSG_HOME"/../
		NMSG_HOME=`pwd`
		CLASSPATH="$NMSG_HOME"/bin
		for x in "$NMSG_HOME"/lib/*
		do
		    CLASSPATH="$CLASSPATH":$x 
		done
	else
		echo "loading lib failed"
		exit 1
	fi
fi
CLASSPATH="$CLASSPATH":"$NMSG_HOME"/lib:"$NMSG_HOME"/conf
JAVA_OPTS=" -server -Xms1280m -Xmx1280m -Xmn384m -XX:SurvivorRatio=1 -XX:PermSize=40m -XX:MaxPermSize=40m -XX:+UseParNewGC  -Xss256k "
if [ "$1" = "run" ]; then

  shift
  "$_RUNJAVA" $JAVA_OPTS $NMSG_OPTS \
    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
    -Dnmsg.base="$NMSG_BASE" \
    -Dnmsg.home="$NMSG_HOME" \
    -Djava.io.tmpdir="$NMSG_TMPDIR" \
    "$_ServerClass" "$@" start

elif [ "$1" = "start" ] ; then
  
  if [ ! -z "$NMSG_PID" ]; then
    if [ -f "$NMSG_PID" ]; then
      if [ -s "$NMSG_PID" ]; then
        echo "Existing PID file found during start."
        if [ -r "$NMSG_PID" ]; then
          PID=`cat "$NMSG_PID"`
          ps -p $PID >/dev/null 2>&1
          if [ $? -eq 0 ] ; then
            echo "Hcps appears to still be running with PID $PID. Start aborted."
            exit 1
          else
            echo "Removing/clearing stale PID file."
            rm -f "$NMSG_PID" >/dev/null 2>&1
            if [ $? != 0 ]; then
              if [ -w "$NMSG_PID" ]; then
                cat /dev/null > "$NMSG_PID"
              else
                echo "Unable to remove or clear stale PID file. Start aborted."
                exit 1
              fi
            fi
          fi
        else
          echo "Unable to read PID file. Start aborted."
          exit 1
        fi
      else
        rm -f "$NMSG_PID" >/dev/null 2>&1
        if [ $? != 0 ]; then
          if [ ! -w "$NMSG_PID" ]; then
            echo "Unable to remove or write to empty PID file. Start aborted."
            exit 1
          fi
        fi
      fi
    fi
  fi
  

  shift
  touch "$NMSG_BASE"/logs/nmsg.out
  "$_RUNJAVA" $JAVA_OPTS $NMSG_OPTS \
    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
    -Dnmsg.base="$NMSG_BASE" \
    -Dnmsg.home="$NMSG_HOME" \
    -Djava.io.tmpdir="$NMSG_TMPDIR" \
    "$_ServerClass" "$@" start \
    >> "$NMSG_BASE"/logs/nmsg.out 2>&1 &

    if [ ! -z "$NMSG_PID" ]; then
      echo $! > $NMSG_PID
    fi
    

elif [ "$1" = "stop" ] ; then

  shift
  FORCE=1
  if [ "$1" = "-force" ]; then
    shift
    FORCE=1
  fi

#  "$_RUNJAVA" $JAVA_OPTS \
#    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
#    -Dnmsg.base="$NMSG_BASE" \
#    -Dnmsg.home="$NMSG_HOME" \
#    -Djava.io.tmpdir="$NMSG_TMPDIR" \
#    "$_ServerClass" "$@" stop

  if [ $FORCE -eq 1 ]; then
    #if [ ! -z "$NMSG_PID" ]; then
    if [ -e $NMSG_PID -a ! -z $NMSG_PID ];then  
       echo "Killing: `cat $NMSG_PID`"
       kill -9 `cat $NMSG_PID`
       RETVAL=$?  
       echo  
       [ $RETVAL = 0 ] && rm -f $NMSG_PID
    else
       echo "Kill failed: \$NMSG_PID not set or don't exist!"
    fi
  fi

elif [ "$1" = "version" ] ; then

    "$_RUNJAVA"   \
      -classpath "$CLASSPATH" \
      com.papple.framework.util.ServerInfo

elif [ "$1" = "hardware" ] ; then

    echo "===========Hardware Status==========="
    echo "CPU core number:        `grep processor /proc/cpuinfo | wc -l` "
    echo `grep MemTotal /proc/meminfo`
    echo "TCP Connections(${PORT_BUSINESS}):        `netstat -an |grep tcp |grep $PORT_BUSINESS |wc -l`"

else

  echo "Usage: nmsg-server.sh ( commands ... )"
  echo "commands:"
  echo "  run               Start nmsg server in the current window"
  echo "  start             Start nmsg server in a separate window"
  echo "  stop              Stop nmsg server"
  echo "  stop -force       Stop nmsg server (followed by kill -KILL)"
  echo "  version           What version of nmsg server server are you running?"
  echo "  hardware          Show hardware info"
  exit 1

fi