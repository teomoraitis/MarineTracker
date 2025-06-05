#!/bin/bash

# set this if you're using a venv
#python_venv_bin="~/producer_venv/bin/python"

selfname="$(basename "$0")"
selfdir="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

launch_separate_tab() {
	echo "launched: $1 [ $2 ]"
	gnome-terminal --tab --wait --title="$1" -- bash -c "$2"
}

start_python_producer() {
	cd "$selfdir/Kafka/dataset"

	if [ -z python_venv_bin ]; then
		launch_separate_tab "python producer" "python3 ./producer.py"
	else
		launch_separate_tab "python producer" "$python_venv_bin ./producer.py"
	fi
}

start_kafka() {
	cd "$selfdir/Kafka/kafka_2.12-3.9.0"
	launch_separate_tab "kafka topic" "./create_topic.sh"
	launch_separate_tab "kafka start" "./start.sh"
}

start_docker() {
	local result="$(docker start postgres_container 2>&1)"
	local permission_denied="$(echo $result | grep -o 'permission denied')"
	
	if [ -n "$permission_denied" ]; then
		# try with sudo?
		
		result="$(sudo docker start postgres_container 2>&1)"
	fi
	
	if [ "$result" != "postgres_container" ]; then
		echo "Docker error:"
		echo "$result"
	fi
}

stop_docker() {
	local result="$(docker stop postgres_container 2>&1)"
	local permission_denied="$(echo $result | grep -o 'permission denied')"
	
	if [ -n "$permission_denied" ]; then
		# try with sudo?
		result="$(sudo docker stop postgres_container 2>&1)"
	fi
	
	if [ "$result" != "postgres_container" ]; then
		echo "Docker error:"
		echo "$result"
	fi
}

start_springboot() {
	sleep 20
	cd "$selfdir/Backend-SpringBoot"
	launch_separate_tab "springboot" "./mvnw clean spring-boot:run; exec bash"
}

handle_sigint() {
	stop_docker
}

trap handle_sigint SIGINT

start_kafka &
start_python_producer &
start_docker &
start_springboot

echo "CTRL+C to stop docker and exit"

sleep infinity

echo "exiting"
