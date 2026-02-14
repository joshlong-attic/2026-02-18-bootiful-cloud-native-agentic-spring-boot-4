#!/usr/bin/env bash
./mvnw -DskipTests package
cf push -p target/scheduler-0.0.1-SNAPSHOT.jar -f manifest.yml