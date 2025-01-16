HOST=$1
PORT=$2
NODE_ID=$3

  RESPONSE=$(curl -X POST "http://$HOST:1$PORT/$NODE_ID/kill")

  if [ $? -eq 0 ]; then
    echo "Node $NODE_ID stopped successfully via API. Response: $RESPONSE"
  else
    echo "Failed to stop Node $NODE_ID via API. Response: $RESPONSE"
  fi
