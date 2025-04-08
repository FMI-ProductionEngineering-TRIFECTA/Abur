#!/bin/bash
set -x

start_default() {
  cd ..
  mkdir -p /workspaces/jenkins_config
  docker compose --profile mongo --profile hello-service up -d
  cd scripts || exit
}

start_jenkins() {
  cd ..
  mkdir -p /workspaces/jenkins_config
  docker compose up -d jenkins
  cd scripts || exit
}

start_mongo() {
  docker compose --profile mongo up -d
}

setup_monitor() {
  cd ..
  mkdir -p infrastructure/prometheus/durable infrastructure/loki/durable
  chmod a+w infrastructure/prometheus/durable infrastructure/loki/durable

  docker plugin install grafana/loki-docker-driver:3.3.2-amd64 --alias loki --grant-all-permissions
  cd scripts || exit
}

start_monitor() {
  setup_monitor
  docker compose --profile monitoring --profile mongo --profile hello-service up -d
}

set +x
if [[ "$1" == "mongo" ]]; then
  start_mongo
elif [[ "$1" == "monitor" ]]; then
  start_monitor
elif [[ "$1" == "jenkins" ]]; then
  start_jenkins
else
  start_default
fi
