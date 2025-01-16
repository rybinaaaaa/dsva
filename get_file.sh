   HOST=$1
   PORT=$2
   NODE_ID=$3

RESPONSE=$(curl "http://$HOST:$PORT/$NODE_ID/file/get")
  echo "Response from node $NODE_ID: $RESPONSE"