#!/bin/bash

docker run -d --name zookeeper -p 2181:2181 \
  -e ALLOW_ANONYMOUS_LOGIN=yes \
  bitnami/zookeeper:3.7

sleep 3

docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092 \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://${EC2_ELASTIC_IP}:9092 \
  -e ALLOW_PLAINTEXT_LISTENER=yes \
  --network bridge \
  bitnami/kafka:3.6
