#!/bin/bash

echo "Stopping any previous ZooKeeper or Kafka..."
./stop.sh

echo "Starting ZooKeeper..."
./bin/zookeeper-server-start.sh config/zookeeper.properties &

echo "Waiting 10 seconds for ZooKeeper to initialize..."
sleep 10

echo "Starting Kafka..."
./bin/kafka-server-start.sh config/server.properties &

echo "Both services started!"
