#!/bin/bash

# Ensure the application is running on port 7070 before executing this script.

BASE_URL="http://localhost:7070"
NODE_PATH="/node"
HEADER="Content-Type: application/json"

# Function for convenient logging
log() {
  echo -e "\n=== $1 ==="
}

log "Adding 10 nodes"
for i in {1..10}; do
  port=$((8080 + i))  # Generate valid port
  curl -X POST -H "$HEADER" -d "{\"hostname\":\"localhost\",\"port\":$port,\"nodeId\":\"$i\"}" $BASE_URL/add_node
  echo -e "\nNode $i with port $port added"
done

log "Fetching information about all nodes"
curl -X GET $BASE_URL/

log "Setting file contents on all nodes"
for i in {1..10}; do
  port=$((8080 + i))  # Generate valid port
  curl -X POST -H "$HEADER" -d "\"Node $i file: Data for node $i\"" $BASE_URL$NODE_PATH/localhost/$port/$i/file/set
done

log "Fetching file contents from all nodes"
for i in {1..10}; do
  port=$((8080 + i))  # Generate valid port
  curl -X GET $BASE_URL$NODE_PATH/localhost/$port/$i/file/get
done

log "Simulating failures of several nodes (node3, node5, node7)"
for i in 3 5 7; do
  port=$((8080 + i))  # Generate valid port
  curl -X POST $BASE_URL$NODE_PATH/localhost/$port/$i/remove
  echo -e "\nNode $i removed"
done

log "Simulating failure of the leader (node1)"
curl -X POST $BASE_URL$NODE_PATH/localhost/8081/1/remove

log "Fetching information about all nodes after node failures"
curl -X GET $BASE_URL/

log "Updating file contents on remaining nodes"
for i in 2 4 6 8 9; do
  port=$((8080 + i))  # Generate valid port
  curl -X POST -H "$HEADER" -d "\"Updated data for node $i\"" $BASE_URL$NODE_PATH/localhost/$port/$i/file/set
done

log "Fetching updated file contents from remaining nodes"
for i in 2 4 6 8 9; do
  port=$((8080 + i))  # Generate valid port
  curl -X GET $BASE_URL$NODE_PATH/localhost/$port/$i/file/get
done

log "Adding new nodes (node11, node12, node13)"
for i in {11..13}; do
  port=$((8080 + i))  # Generate valid port
  curl -X POST -H "$HEADER" -d "{\"hostname\":\"localhost\",\"port\":$port,\"nodeId\":\"$i\"}" $BASE_URL/add_node
  echo -e "\nNode $i with port $port added"
done

log "Removing the leader (node10)"
curl -X POST $BASE_URL$NODE_PATH/localhost/8090/10/remove

log "Fetching information about all nodes after adding new ones"
curl -X GET $BASE_URL/

log "Setting file contents on new nodes"
for i in {11..13}; do
  port=$((8080 + i))  # Generate valid port
  curl -X POST -H "$HEADER" -d "\"Node $i file: New data\"" $BASE_URL$NODE_PATH/localhost/$port/$i/file/set
done

log "Fetching file contents from new nodes"
for i in {11..13}; do
  port=$((8080 + i))  # Generate valid port
  curl -X GET $BASE_URL$NODE_PATH/localhost/$port/$i/file/get
done

log "Simulating a mass failure of nodes (node2, node4, node8, node9)"
for i in 2 4 8 9; do
  port=$((8080 + i))  # Generate valid port
  curl -X POST $BASE_URL$NODE_PATH/localhost/$port/$i/remove
  echo -e "\nNode $i removed"
done

log "Fetching information about all nodes after mass failures"
curl -X GET $BASE_URL/

log "Ending the simulation"