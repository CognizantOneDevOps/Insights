if [ $# -eq 0 ]; then
    echo "No argument"
fi
mq_ip=$1
echo "MQ STATUS CHECK" >> mq.log
echo "TimeStamp : " $(date '+%F %T')""   >> mq.log
echo "IP: ${mq_ip}, PORT : 15672"
mq_up="curl -I http://${mq_ip}:15672/"
RESPONSE=`$mq_up`
if [[ $RESPONSE = *"HTTP/1.1 200 OK"* ]]; then
  echo "MQ is up and running in ${mq_ip}:15672" >> mq.log
else
  echo "MQ is down with ${mq_ip}:15672" >> mq.log
  exit 1
fi


mq_test="curl -X GET -u iSight:iSight http://${mq_ip}:15672/api/queues"
queue_limit=$2
RESPONSE=`$mq_test`
queue_count=0
for row in $(echo "${RESPONSE}" | jq -r '.[] | @base64'); do
    _jq() {
     echo ${row} | base64 --decode | jq -r ${1}
    }
    queue_count=$((queue_count+1))
done
echo "  Total number of queues: "${queue_count} >> mq.log




for row in $(echo "${RESPONSE}" | jq -r '.[] | @base64'); do
    _jq() {
     echo ${row} | base64 --decode | jq -r ${1}
    }

   queue_size=$(_jq '.messages')
   if [[ $queue_size -gt $queue_limit ]]; then
       echo "   Name: "$(_jq '.name') ", Size: "$queue_size>> mq.log
       echo "   This is more than expected queue size which is ${queue_limit}" >> mq.log
   fi
done
