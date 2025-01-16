#!/bin/bash

if [ $# -lt 3 ]; then
  echo "Usage: $0 <host> <port> <nodeId> [toHost toPort toNodeId]"
  exit 1
fi

# New node info
HOST=$1
PORT=$2
NODE_ID=$3

# Bind to node below
TO_HOST=${4:-""}
TO_PORT=${5:-""}
TO_NODE_ID=${6:-""}

JAR_PATH="target/dsva2-1.0-SNAPSHOT.jar"


if [ -z "$TO_HOST" ] || [ -z "$TO_PORT" ] || [ -z "$TO_NODE_ID" ]; then
  # Add Single Node
  echo "Starting single node with host=$HOST, port=$PORT, nodeId=$NODE_ID"
  java -Dhost="$HOST" -Dport="$PORT" -DnodeId="$NODE_ID" -jar "$JAR_PATH"
else
  # Bind to existing node
  echo "Starting node with host=$HOST, port=$PORT, nodeId=$NODE_ID, connecting to toHost=$TO_HOST, toPort=$TO_PORT, toNodeId=$TO_NODE_ID"
  java -Dhost="$HOST" -Dport="$PORT" -DnodeId="$NODE_ID" \
       -DtoHost="$TO_HOST" -DtoPort="$TO_PORT" -DtoNodeId="$TO_NODE_ID" \
       -jar "$JAR_PATH"
fi
