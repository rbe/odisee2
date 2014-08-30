#!/bin/sh

# Common settings
JVM_OPTS="-Djava.awt.headless=true -Dfile.encoding=UTF-8 -server"
TOMCAT_OPTS="-Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false -Ddisable.auto.recompile=true"

# Memory settings
# -Xms -Xmx
MEM_OPTS="-Xms512m -Xmx512m"
# Young generation
# -XX:NewSize= -XX:MaxNewSize=
YOUNGEN_OPTS="-Xmn256m"
# Permanent generation
PERMGEN_OPTS="-XX:PermSize=64m -XX:MaxPermSize=64m"
# Stack size
STACK_OPTS="-XX:ThreadStackSize=128k"

# Error handling
# OutOfMemoryError
# -XX:OnOutOfMemoryError=\"\""
OOM_ERROR="-XX:-HeapDumpOnOutOfMemoryError"
# JVM error
# -XX:OnError=\"<cmd args>;<cmd args>\""
ON_ERROR=""

# Performance optimization
# -XX:+CMSClassUnloadingEnabled
# -XX:+UseCompressedStrings -XX:+OptimizeStringConcat were introduced in Java 6u20 and u21
OPTIM_OPTS="-XX:+UseStringCache -XX:+OptimizeStringConcat" 

# Garbage Collection
#GC_OPTS="-XX:+DisableExplicitGC"
#GC_DEBUG_OPTS="-Xloggc:$CATALINA_HOME/logs/gc.log -XX:+PrintHeapAtGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
#GC_LOGROTATE="-XX:-UseGCLogRotation -XX:NumberOfGClogFiles=100 -XX:GCLogFileSize=1M"

# Management
#MGMT_OPTS="-Dcom.sun.management.jmxremote" 

# Extra options
#
# In case of ClassNotFoundException, e.g. [Ljava.lang.String; see Bug 6434149, http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6434149
#-Dsun.lang.ClassLoader.allowArraySyntax=true
EXTRA_OPTS=""

JAVA_OPTS="$JVM_OPTS $MEM_OPTS $STACK_OPTS $YOUNGEN_OPTS $PERMGEN_OPTS $OPTIM_OPTS $TOMCAT_OPTS $MGMT_OPTS $GC_OPTS $GC_DEBUG_OPTS $OOM_ERROR $ON_ERROR $EXTRA_OPTS"
