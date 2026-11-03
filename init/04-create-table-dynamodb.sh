#!/bin/bash
echo ">>> running create-table-dynamodb.sh"
awslocal dynamodb create-table --table-name oms-document-processing --attribute-definitions "AttributeName=documentKey,AttributeType=S" "AttributeName=step,AttributeType=S" --key-schema "AttributeName=documentKey,KeyType=HASH" "AttributeName=step,KeyType=RANGE" --billing-mode PAY_PER_REQUEST