#!/bin/ksh

. /apps/home/appadmin/.profile

###############################################################################
#
# Activation.sh
#      This shell is designed to retrieve the list of customers that have 
#      recently made a payment to show confirmation that their payment
#      has been received.
#
###############################################################################

date=`date +%Y%m%d%H%M`
logdate=`date +%Y%m%d`

HOME=/apps/home/appadmin

currentDir=$PWD
classpath=${HOME}/SUNWappserver/lib/javaee.jar:${HOME}/telscape/lib/smppapi-0.3.7.jar:${HOME}/telscape/lib/log4j-1.2.5.jar:${HOME}/telscape/lib/ojdbc14.jar:${HOME}/telscape/lib/commons-logging.jar:${HOME}/telscape/lib/appserv-ws.jar:${HOME}/telscape/lib/mvno_api_proxy.jar:${CLASSPATH}
log=${HOME}/telscape/logs/PromCapabilitySmApp_${logdate}.log

echo CLASSPATH=${classpath}


cd ${HOME}/telscape/projects/PromCapabilitySmApp

/apps/home/appadmin/SUNWappserver/jdk/bin/java -cp ${classpath}:${HOME}/telscape/projects/PromCapabilitySmApp/com/tscp/mvno/smpp/:${HOME}/telscape/projects/PromCapabilitySmApp/com/tscp/mvno/smpp/db/ com.tscp.mvno.smpp.SMSMessageProcessor >> ${log}
