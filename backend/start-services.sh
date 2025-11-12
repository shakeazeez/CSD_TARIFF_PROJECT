#!/bin/bash

# Function to start a service
start_service() {
    local jar=$1
    local port=$2
    echo "Starting $jar on port $port"
    java -jar $jar &
    local pid=$!
    echo "$jar started with PID $pid"
    eval "${jar}_pid=$pid"
}

# Function to check if service is running
check_service() {
    local pid_var=$1
    local pid=$(eval echo \$$pid_var)
    if [ -n "$pid" ] && kill -0 $pid 2>/dev/null; then
        return 0
    else
        return 1
    fi
}

# Function to restart service
restart_service() {
    local jar=$1
    local port=$2
    local pid_var=$3
    echo "$(date): $jar crashed or not responding, restarting..."
    if check_service $pid_var; then
        kill $(eval echo \$$pid_var)
        sleep 2
    fi
    start_service $jar $port
}

# Start all services
start_service tariffCalc.jar 8081
start_service user.jar 8082
start_service news.jar 8083

# Monitor loop
while true; do
    sleep 30  # Check every 30 seconds

    # Check tariffCalc
    if ! check_service tariffCalc_pid; then
        restart_service tariffCalc.jar 8081 tariffCalc_pid
    fi

    # Check user
    if ! check_service user_pid; then
        restart_service user.jar 8082 user_pid
    fi

    # Check news
    if ! check_service news_pid; then
        restart_service news.jar 8083 news_pid
    fi
done