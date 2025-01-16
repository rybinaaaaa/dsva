#!/bin/bash

./run.sh 127.0.0.1 3331 1 &
sleep 3
./run.sh 127.0.0.1 3332 2 127.0.0.1 3331 1 &
sleep 3
./run.sh 127.0.0.1 3333 3 127.0.0.1 3332 2 &
sleep 3
./run.sh 127.0.0.1 3339 9 127.0.0.1 3332 2 &
sleep 3
./run.sh 127.0.0.1 3334 4 127.0.0.1 3333 3 &

sleep 1

./get_file.sh "127.0.0.1" 3331 1
sleep 3

./set_file.sh "127.0.0.1" 3333 3 "Node 3 has changed file"
sleep 3


./kill.sh "127.0.0.1" 3331 1
sleep 2

./run.sh 127.0.0.1 3337 7 127.0.0.1 3333 3
sleep 1

./info.sh 127.0.0.1 3337 7 &


echo "Simulation completed."
