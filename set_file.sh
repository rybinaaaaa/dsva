   HOST=$1
   PORT=$2
   NODE_ID=$3
   CONTENT=$4

RESPONSE=$(curl -X POST -H "Content-Type: application/json" -d "\"$CONTENT\"" "http://$HOST:1$PORT/$NODE_ID/file/set")


echo "Response from node $NODE_ID: $RESPONSE"