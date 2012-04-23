#!/bin/ksh

. /apps/home/appadmin/.profile

HOME=/apps/home/appadmin

classpath=${CLASSPATH}
classpath=${classpath}:${HOME}/SUNWappserver/lib/javaee.jar
classpath=${classpath}:${HOME}/telscape/lib/smppapi-0.3.7.jar
classpath=${classpath}:${HOME}/telscape/lib/log4j-1.2.5.jar
classpath=${classpath}:${HOME}/telscape/lib/ojdbc14.jar
classpath=${classpath}:${HOME}/telscape/lib/commons-logging.jar
classpath=${classpath}:${HOME}/telscape/lib/appserv-ws.jar
classpath=${classpath}:${HOME}/telscape/lib/mvno_api_proxy.jar
classpath=${classpath}:${HOME}/telscape/projects/PromCapabilitySmApp/com/tscp/mvno/smpp/
classpath=${classpath}:${HOME}/telscape/projects/PromCapabilitySmApp/com/tscp/mvno/smpp/db/

echo ${classpath}

javac -classpath ${classpath} $1
