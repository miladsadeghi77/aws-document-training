#!/bin/bash
echo ">>> running create-sns.sh"
# Create SNS topic
awslocal sns create-topic --name oms-document-uploaded

# Subscribe oms-documents
awslocal sns subscribe --topic-arn arn:aws:sns:us-east-1:000000000000:oms-document-uploaded  --protocol sqs  --notification-endpoint arn:aws:sqs:us-east-1:000000000000:oms-documents

# Subscribe oms-email-notifications
awslocal sns subscribe --topic-arn arn:aws:sns:us-east-1:000000000000:oms-document-uploaded --protocol sqs --notification-endpoint arn:aws:sqs:us-east-1:000000000000:oms-email-notifications

# Subscribe oms-analytics
awslocal sns subscribe --topic-arn arn:aws:sns:us-east-1:000000000000:oms-document-uploaded  --protocol sqs  --notification-endpoint arn:aws:sqs:us-east-1:000000000000:oms-analytics

# Verify subscriptions
awslocal sns list-subscriptions-by-topic --topic-arn arn:aws:sns:us-east-1:000000000000:oms-document-uploaded