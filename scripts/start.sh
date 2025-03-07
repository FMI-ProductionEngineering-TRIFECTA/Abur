#!/bin/bash
set -x

cd ..
mkdir -p /workspaces/jenkins_config
docker compose --profile mongo --profile hello-service up -d 
cd scripts
