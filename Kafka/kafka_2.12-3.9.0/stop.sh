#!/bin/bash

echo "Stopping Kafka..."
./bin/kafka-server-stop.sh

echo "Waiting for Kafka to stop..."
sleep 3

echo "Stopping ZooKeeper..."
./bin/zookeeper-server-stop.sh

echo "Waiting for ZooKeeper to stop..."
sleep 3

echo "All services stopped."
