rem Common settings
set JVM_OPTS=-Djava.awt.headless=true -Dfile.encoding=UTF-8 -server
set TOMCAT_OPTS=-Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false -Ddisable.auto.recompile=true

rem Memory settings
rem -Xms -Xmx
set MEM_OPTS=
set STACK_OPTS=-XX:ThreadStackSize=128k
rem -XX:NewSize= -XX:MaxNewSize=
set YOUNGEN_OPTS=-Xmn256m
set PERMGEN_OPTS=-XX:PermSize=64m -XX:MaxPermSize=64m

rem Error handling
rem -XX:OnOutOfMemoryError="<cmd args>"
set OOM_ERROR=-XX:-HeapDumpOnOutOfMemoryError
rem ON_ERROR=-XX:OnError="<cmd args>;<cmd args>"
set ON_ERROR=

rem Performance optimization
rem -XX:+CMSClassUnloadingEnabled
rem -XX:+UseCompressedStrings -XX:+OptimizeStringConcat were introduced in Java 6u20 and u21
set OPTIM_OPTS="-XX:+UseStringCache -XX:+OptimizeStringConcat" # -XX:+UseCompressedStrings

rem Garbage Collection
rem set GC_OPTS="-XX:+DisableExplicitGC"
rem set GC_DEBUG_OPTS="-Xloggc:$CATALINA_HOME/logs/gc.log -XX:+PrintHeapAtGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
rem set GC_LOGROTATE="-XX:-UseGCLogRotation -XX:NumberOfGClogFiles=100 -XX:GCLogFileSize=1M"

rem Management
rem MGMT_OPTS="-Dcom.sun.management.jmxremote" 

set JAVA_OPTS=%JVM_OPTS% %MEM_OPTS% %STACK_OPTS% %YOUNGEN_OPTS% %PERMGEN_OPTS% %OPTIM_OPTS% %TOMCAT_OPTS% %MGMT_OPTS% %GC_OPTS% %GC_DEBUG_OPTS% %OOM_ERROR% %ON_ERROR%
