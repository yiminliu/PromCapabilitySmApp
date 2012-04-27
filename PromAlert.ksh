#! /bin/ksh
# Program Name: PromAlert.ksh
# Author: IT Dept (Yimin/Juan)- Apr 24, 2012
export HOME=/apps/home/appadmin
. ${HOME}/.profile

error_message(){
if [ $# -lt 1 ] || [ $# -gt 2 ]; then
   echo "No argument sent.\n\nUsage:\n\terror_message \[message_text\|filename\] \{F\|W default Fatal\}">/tmp/error_message.$$
elif [ "x$1" = "x" ]; then
   echo "Null argument 1. \n\nUsage:\n\terror_message \[message_text\|filename\] \{F\|W default Fatal\}">/tmp/error_message.$$
else
   if [ ! -f "$1" ]; then
      echo "$1" > /tmp/error_message.$$
   else
      if [ -s "$1" ]; then
         mv "$1" /tmp/error_message.$$
      else
         echo "File is empty.\n\nUsage:\n\terror_message \[message_text\|filename\]">/tmp/error_message.$$
      fi
   fi
fi
#mailx -s "Error in $0 at $(hostname)" -r tscdba@telscape.net -c tscdba@telscape.net promo_alerts@telscape.net < /tmp/error_message.$$
rm /tmp/error_message.$$
if [ "x$2" = "x" ] || [ "x$2" = "xF" ]; then
   exit 1
fi
}

# main
logdate=`date +%Y%m%d`

classpath=${HOME}/SUNWappserver/lib/javaee.jar:${HOME}/telscape/lib/smppapi-0.3.7.jar:${HOME}/telscape/lib/log4j-1.2.5.jar:${HOME}/telscape/lib/ojdbc14.jar:${HOME}/telscape/lib/commons-logging.jar:${HOME}/telscape/lib/appserv-ws.jar:${HOME}/telscape/lib/mvno_api_proxy.jar:${CLASSPATH}
log=${HOME}/telscape/logs/promAlertSmpp_${logdate}.log
error_log=${HOME}/telscape/logs/promAlertSmpp_${logdate}.err

export CLASSPATH=${classpath}

if [ ! -d ${HOME}/telscape/projects/promAlertSmpp ]; then
   error_message "Directory ${HOME}/telscape/projects/promAlertSmpp does not exist"
fi
cd ${HOME}/telscape/projects/promAlertSmpp
${HOME}/SUNWappserver/jdk/bin/java -cp ${classpath}:${HOME}/telscape/projects/promAlertSmpp/ com.tscp.mvno.smpp.SMSMessageProcessor 1>${log} 2>${error_log}
if [ $? -ne 0 ] || [ -s ${error_log} ]; then
   cat ${log} ${error_log} > /tmp/$$.tmp
   error_message /tmp/$$.tmp
fi
if [ -f ${error_log} ]; then
   rm ${error_log}
fi
exit 0
