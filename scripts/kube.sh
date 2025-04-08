#!/bin/bash
set -x

init() {
  cd ../infrastructure/kubernetes
  kubectl apply -f mongo.yaml
  kubectl apply -f hello.yaml
  cd ../../scripts
}

start() {
  minikube start
}

info() {
  kubectl get deployments
}

start_tunnel() {
  (minikube service hello --url | head -n 1 | tee /dev/tty | xclip -selection clipboard) & sleep 3; kill $!
}

list_tunnels() {
  pgrep -af "minikube service hello --url"
}

kill_tunnels() {
  pkill -f "minikube service hello --url"
}

stop() {
  kill_tunnels
  minikube stop
}

clear() {
  kubectl delete deploy hello
  kubectl delete deploy mongo
}

set +x
if [[ "$1" == "init" ]]; then
  init
elif [[ "$1" == "start" ]]; then
  start
elif [[ "$1" == "list" ]]; then
  list_tunnels
elif [[ "$1" == "kill" ]]; then
  kill_tunnels
elif [[ "$1" == "stop" ]]; then
  stop
elif [[ "$1" == "clear" ]]; then
  stop
else
  start_tunnel
fi
