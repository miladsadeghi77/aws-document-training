#!/bin/bash
set -e
echo ">>> running create-sqs-queues.sh"

DLQ_URL=$(awslocal sqs create-queue \
  --queue-name oms-documents-dlq \
  --query QueueUrl --output text)

DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url "$DLQ_URL" \
  --attribute-names QueueArn \
  --query Attributes.QueueArn --output text)

REDRIVE="{\"deadLetterTargetArn\":\"$DLQ_ARN\",\"maxReceiveCount\":\"3\"}"

awslocal sqs create-queue \
  --queue-name oms-documents \
  --attributes "RedrivePolicy='$REDRIVE'"

awslocal sqs create-queue --queue-name oms-email-notifications
awslocal sqs create-queue --queue-name oms-analytics