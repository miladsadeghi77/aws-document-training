awslocal sqs create-queue --queue-name oms-documents-dlq
awslocal sqs get-queue-attributes `
  --queue-url http://localhost:4566/000000000000/oms-documents-dlq
  --attribute-names QueueArn`

awslocal sqs create-queue `
  --queue-name oms-documents `
  --attributes '{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"arn:aws:sqs:us-east-1:000000000000:oms-documents-dlq\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\"}'

awslocal sqs create-queue --queue-name oms-email-notifications
awslocal sqs create-queue --queue-name oms-analytics