#!/usr/bin/env bash
rm -rf target
./mvnw -DskipTests package
cf push -p target/assistant-0.0.1-SNAPSHOT.jar -f manifest.yml
cf logs adoptions-assistant --recent