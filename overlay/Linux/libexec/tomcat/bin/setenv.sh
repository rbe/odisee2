#!/bin/sh
#
# Odisee(R)
# Copyright (C) 2011-2014 art of coding UG (haftungsbeschränkt).
# Copyright (C) 2005-2010 Informationssysteme Ralf Bensmann.
#
# Alle Rechte vorbehalten. Nutzung unterliegt Lizenzbedingungen.
# All rights reserved. Use is subject to license terms.
#

# Common settings
ODISEE_JVM_OPTS="-Djava.awt.headless=true -Dfile.encoding=UTF-8 -server"
ODISEE_TOMCAT_OPTS="-Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false -Ddisable.auto.recompile=true"

# Memory settings
# -Xms -Xmx
#ODISEE_MEM_OPTS="-Xms512m -Xmx512m"
ODISEE_MEM_OPTS=""
# Young generation
# -XX:NewSize= -XX:MaxNewSize=
#ODISEE_YOUNGEN_OPTS="-Xmn256m"
ODISEE_YOUNGEN_OPTS=""
# Permanent generation
#ODISEE_PERMGEN_OPTS="-XX:PermSize=64m -XX:MaxPermSize=64m"
ODISEE_PERMGEN_OPTS=""
# Stack size
#ODISEE_STACK_OPTS="-XX:ThreadStackSize=128k"
ODISEE_STACK_OPTS=""

# Error handling
# OutOfMemoryError
# -XX:OnOutOfMemoryError=\"\""
ODISEE_OOM_ERROR="-XX:-HeapDumpOnOutOfMemoryError"
# JVM error
# -XX:OnError=\"<cmd args>;<cmd args>\""
ODISEE_ON_ERROR=""

# Performance optimization
# -XX:+CMSClassUnloadingEnabled
# -XX:+UseCompressedStrings -XX:+OptimizeStringConcat were introduced in Java 6u20 and u21
ODISEE_OPTIM_OPTS="-XX:+UseStringCache -XX:+OptimizeStringConcat" 

# Garbage Collection
#GC_OPTS="-XX:+DisableExplicitGC"
ODISEE_GC_OPTS=""
#GC_DEBUG_OPTS="-Xloggc:$CATALINA_HOME/logs/gc.log -XX:+PrintHeapAtGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
ODISEE_GC_DEBUG_OPTS=""
#GC_LOGROTATE="-XX:-UseGCLogRotation -XX:NumberOfGClogFiles=100 -XX:GCLogFileSize=1M"
ODISEE_GC_LOGROTATE=""

# Management
#MGMT_OPTS="-Dcom.sun.management.jmxremote" 
ODISEE_MGMT_OPTS=""

# Extra options
#
# In case of ClassNotFoundException, e.g. [Ljava.lang.String; see Bug 6434149, http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6434149
#-Dsun.lang.ClassLoader.allowArraySyntax=true
ODISEE_EXTRA_OPTS=""

JAVA_OPTS="$ODISEE_JVM_OPTS $ODISEE_MEM_OPTS $ODISEE_STACK_OPTS $ODISEE_YOUNGEN_OPTS $ODISEE_PERMGEN_OPTS $ODISEE_OPTIM_OPTS $ODISEE_TOMCAT_OPTS $ODISEE_MGMT_OPTS $ODISEE_GC_OPTS $ODISEE_GC_DEBUG_OPTS $ODISEE_OOM_ERROR $ODISEE_ON_ERROR $ODISEE_EXTRA_OPTS"
