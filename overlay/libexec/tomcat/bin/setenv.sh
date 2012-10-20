#!/bin/sh

JVM_OPTS="-Djava.awt.headless=true -Dfile.encoding=UTF-8 -server"
TOMCAT_OPTS="-Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false -Ddisable.auto.recompile=true"

# Memory settings
MEM_OPTS="-Xms128m -Xmx512m"
STACK_OPTS="-XX:ThreadStackSize=256"
YOUNGEN_OPTS="-XX:NewSize=64m -XX:MaxNewSize=64m"
PERMGEN_OPTS="-XX:PermSize=128m -XX:MaxPermSize=128m"

# Error handling
##OOMERROR="-XX:-HeapDumpOnOutOfMemoryError -XX:OnOutOfMemoryError=\"\""
##ONERROR="-XX:OnError=\"<cmd args>;<cmd args>\""

# Performance optimization
# -XX:+CMSClassUnloadingEnabled
# -XX:+UseCompressedStrings -XX:+OptimizeStringConcat were introduced in Java 6u20 and u21
OPTIM_OPTS="-XX:+UseStringCache -XX:+UseCompressedStrings -XX:+OptimizeStringConcat"

# Garbage Collection
GC_OPTS="-XX:+DisableExplicitGC"
GC_DEBUG_OPTS="-Xloggc:$CATALINA_HOME/logs/gc.log -XX:+PrintHeapAtGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
GC_LOGROTATE="-XX:-UseGCLogRotation -XX:NumberOfGClogFiles=100 -XX:GCLogFileSize=1M"

JAVA_OPTS="$JVM_OPTS $MEM_OPTS $STACK_OPTS $YOUNGEN_OPTS $PERMGEN_OPTS $OPTIM_OPTS $TOMCAT_OPTS $GC_OPTS $GC_DEBUG_OPTS"
