:: Common settings
set ODISEE_JVM_OPTS=-Djava.awt.headless=true -Dfile.encoding=UTF-8 -server
set ODISEE_TOMCAT_OPTS=-Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false -Ddisable.auto.recompile=true

:: Memory settings
:: -Xms -Xmx
set ODISEE_MEM_OPTS= 
set ODISEE_STACK_OPTS=-XX:ThreadStackSize=128k
:: -XX:NewSize= -XX:MaxNewSize=
set ODISEE_YOUNGEN_OPTS=-Xmn256m
set ODISEE_PERMGEN_OPTS=-XX:PermSize=64m -XX:MaxPermSize=64m

:: Error handling
:: -XX:OnOutOfMemoryError="<cmd args>"
set ODISEE_OOM_ERROR=-XX:+HeapDumpOnOutOfMemoryError
:: ON_ERROR=-XX:OnError="<cmd args>;<cmd args>"
set ODISEE_ON_ERROR=

:: Performance optimization
:: -XX:+CMSClassUnloadingEnabled
:: -XX:+UseCompressedStrings -XX:+OptimizeStringConcat were introduced in Java 6u20 and u21
set ODISEE_OPTIM_OPTS="-XX:+UseStringCache -XX:+OptimizeStringConcat" # -XX:+UseCompressedStrings

:: Garbage Collection
set ODISEE_GC_OPTS="-XX:+DisableExplicitGC"
set ODISEE_GC_DEBUG_OPTS="-Xloggc:$CATALINA_HOME/logs/gc.log -XX:+PrintHeapAtGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
set ODISEE_GC_LOGROTATE="-XX:-UseGCLogRotation -XX:NumberOfGClogFiles=100 -XX:GCLogFileSize=1M"

:: Management
ODISEE_MGMT_OPTS="-Dcom.sun.management.jmxremote" 

set JAVA_OPTS=%ODISEE_JVM_OPTS% %ODISEE_MEM_OPTS% %ODISEE_STACK_OPTS% %ODISEE_YOUNGEN_OPTS% %ODISEE_PERMGEN_OPTS% %ODISEE_OPTIM_OPTS% %ODISEE_TOMCAT_OPTS% %ODISEE_MGMT_OPTS% %ODISEE_GC_OPTS% %ODISEE_GC_DEBUG_OPTS% %ODISEE_OOM_ERROR% %ODISEE_ON_ERROR%
