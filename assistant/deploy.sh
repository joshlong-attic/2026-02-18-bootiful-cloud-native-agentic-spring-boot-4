#!/usr/bin/env bash
./mvnw -DskipTests package
cf push -p target/assistant-0.0.1-SNAPSHOT.jar -f manifest.yml