#!/bin/bash
set -x

stop_default() {
  docker compose --profile mongo --profile hello-service down
}

stop_mongo() {
  docker compose --profile mongo down
}

stop_monitor() {
  docker compose --profile monitoring --profile mongo --profile hello-service down
}

set +x
if [[ "$1" == "mongo" ]]; then
  stop_mongo
elif [[ "$1" == "monitor" ]]; then
  stop_monitor
else
  stop_default
fi

