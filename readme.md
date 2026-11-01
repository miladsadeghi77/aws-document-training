# AWS Document Training

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![AWS SDK](https://img.shields.io/badge/AWS%20SDK-v2-yellow)
![LocalStack](https://img.shields.io/badge/LocalStack-enabled-blue)

A production-style Spring Boot application built as part of a structured AWS learning course for Java backend developers.

The project integrates with AWS services using **AWS SDK v2**, developed and tested locally with **LocalStack**, so no real AWS account is required.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [AWS Services Covered](#aws-services-covered)
- [Architecture](#architecture)
- [Features](#features)
- [Project Structure](#project-structure)
- [Local Setup](#local-setup)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Quick Test](#quick-test)
- [Key Design Decisions](#key-design-decisions)
- [Troubleshooting](#troubleshooting)
- [Course Progress](#course-progress)

---

## Tech Stack

| Layer            | Technology              |
|------------------|-------------------------|
| Language         | Java 21                 |
| Framework        | Spring Boot 4.1.1       |
| AWS SDK          | AWS SDK v2 (2.25.40)    |
| Local AWS        | LocalStack              |
| Containerization | Docker / Docker Compose |
| Build            | Maven                   |
| Utilities        | Lombok, Jackson         |

---

## AWS Services Covered

| Service                                             | Status                |
|-----------------------------------------------------|-----------------------|
| S3: Upload, Download, Streaming, Presigned URLs     | ✅ Complete            |
| SQS: Async messaging, DLQ, Long polling             | ✅ Complete            |
| SNS: Pub/Sub, Fan-out pattern, Envelope unwrapping  | ✅ Complete            |
| Event-driven patterns: Outbox, Correlation IDs/MDC  | ✅ Complete            |
| DynamoDB                                            | 🔄 Next               |
| RDS with Spring Boot                                | ⬜ Pending             |
| Spring Cloud AWS                                    | ⬜ Pending             |
| Secrets Manager                                     | ⬜ Pending             |
| Parameter Store                                     | ⬜ Pending             |
| Lambda                                              | ⬜ Pending             |
| API Gateway                                         | ⬜ Pending             |
| CloudWatch                                          | ⬜ Pending             |

-----------------------------------------------------|-------------|
| S3: Upload, Download, Streaming, Presigned URLs     | ✅ Complete  |
| SQS: Async messaging, DLQ, Long polling             | ✅ Complete  |
| SNS: Pub/Sub, Fan-out pattern, Envelope unwrapping  | ✅ Complete  |
| DynamoDB                                            | 🔄 Upcoming |
| Secrets Manager                                     | 🔄 Upcoming |
| Parameter Store                                     | 🔄 Upcoming |
| Lambda                                              | 🔄 Upcoming |
| CloudWatch                                          | 🔄 Upcoming |

---

## Architecture

```
HTTP Client
     │
     ▼
DocumentController
     │
     ▼
DocumentUseCase  ──────────────────────────────────────────┐
     │                                                     │
     ▼                                                     ▼
S3StorageService                                 SnsMessagePublisher
     │                                                     │
     ▼                                                     ▼
LocalStack S3                                    SNS Topic: oms-document-uploaded
(oms-documents bucket)                                     │
                                          ┌────────────────┼──────────────────┐
                                          ▼                ▼                  ▼
                                     oms-documents  oms-email-notifications  oms-analytics
                                          │
                                          ▼
                                 DocumentProcessingConsumer
                                 (@Scheduled, polls every 5s)
                                          │
                                          ▼
                                 DocumentProcessor
                                 (fetches S3 metadata, logs report)
```

---

## Features

### S3

- Upload files via `POST /api/documents/upload`
- Stream download via `GET /api/documents/download/**`
- Generate presigned download URL via `GET /api/documents/presigned-download-url`
- Generate presigned upload URL via `GET /api/documents/presigned-upload-url`

### SQS + SNS

- After upload, an event is published to the SNS topic `oms-document-uploaded`
- SNS fans out to three SQS queues simultaneously
- A background consumer polls the `oms-documents` queue every 5 seconds
- The SNS envelope is unwrapped automatically before processing
- Failed messages retry up to 3 times, then move to the Dead Letter Queue
- Poison messages (deserialization failures) are deleted immediately

---

## Project Structure

```
src/main/java/com/miladsadeghi/awsdocument/
├── api/
│   ├── Controller/
│   │   └── DocumentController.java
│   └── error/
│       └── ErrorHandler.java
├── domain/
│   ├── exception/
│   │   ├── DocumentNotFoundException.java
│   │   └── StorageException.java
│   ├── model/
│   │   ├── DocumentUploadedEvent.java
│   │   ├── FileDownloadResult.java
│   │   └── DocumentUploadResponse.java
│   ├── service/
│   │   ├── S3StorageService.java
│   │   └── DocumentProcessor.java
│   └── usecase/
│       └── DocumentUseCase.java
└── infrastructure/
    ├── config/
    │   ├── S3Config.java
    │   ├── S3PresignerConfig.java
    │   ├── SqsConfig.java
    │   └── SnsConfig.java
    └── sqs/
        ├── consumer/
        │   └── DocumentProcessingConsumer.java
        ├── publisher/
        │   ├── SqsMessagePublisher.java
        │   └── SnsMessagePublisher.java
        └── model/
            └── SnsEnvelope.java
```

Outside `src/`, `init/` holds the LocalStack resource setup and `docker-compose.yml` starts LocalStack.

The layers follow a clean-architecture style: `api` handles HTTP, `domain` holds business logic and has no AWS dependencies in its models, and `infrastructure` contains AWS wiring and messaging.

---

## Local Setup

### Prerequisites

- Java 21
- Docker Desktop
- AWS CLI
- Maven

### 1. Start LocalStack

```bash
docker compose up -d
```

The `init/` folder contains the LocalStack setup for this project: the S3 buckets, the SQS queues (including the DLQ), and the SNS topic with its queue subscriptions. Steps 4 to 6 below are the equivalent manual commands, useful if you want to understand what the setup creates or need to recreate a single resource.

### 2. Configure dummy AWS credentials

```bash
aws configure
# Access Key ID:     test
# Secret Access Key: test
# Region:            us-east-1
# Output format:     json
```

### 3. Set up the `awslocal` alias

**PowerShell**

```powershell
function awslocal { aws --endpoint-url http://localhost:4566 $args }
```

**Bash / Zsh**

```bash
alias awslocal='aws --endpoint-url http://localhost:4566'
```

### 4. Create S3 buckets (manual alternative to `init/`)

```bash
awslocal s3 mb s3://oms-documents
awslocal s3 mb s3://oms-reports
```

### 5. Create SQS queues

**PowerShell**

```powershell
# Dead Letter Queue first
awslocal sqs create-queue --queue-name oms-documents-dlq

# Main queue with DLQ attached
awslocal sqs create-queue `
  --queue-name oms-documents `
  --attributes '{"RedrivePolicy":"{\"deadLetterTargetArn\":\"arn:aws:sqs:us-east-1:000000000000:oms-documents-dlq\",\"maxReceiveCount\":\"3\"}"}'

# Additional queues for fan-out
awslocal sqs create-queue --queue-name oms-email-notifications
awslocal sqs create-queue --queue-name oms-analytics
```

**Bash / Zsh** (use `\` for line continuation instead of the backtick)

```bash
awslocal sqs create-queue --queue-name oms-documents-dlq

awslocal sqs create-queue \
  --queue-name oms-documents \
  --attributes '{"RedrivePolicy":"{\"deadLetterTargetArn\":\"arn:aws:sqs:us-east-1:000000000000:oms-documents-dlq\",\"maxReceiveCount\":\"3\"}"}'

awslocal sqs create-queue --queue-name oms-email-notifications
awslocal sqs create-queue --queue-name oms-analytics
```

### 6. Create SNS topic and subscribe queues

```bash
awslocal sns create-topic --name oms-document-uploaded

for q in oms-documents oms-email-notifications oms-analytics; do
  awslocal sns subscribe \
    --topic-arn arn:aws:sns:us-east-1:000000000000:oms-document-uploaded \
    --protocol sqs \
    --notification-endpoint arn:aws:sqs:us-east-1:000000000000:$q
done
```

On PowerShell, run the `awslocal sns subscribe` command once per queue, using the same flags with a backtick for line continuation.

### 7. Run the application

**PowerShell**

```powershell
$env:AWS_ENDPOINT="http://localhost:4566"
./mvnw spring-boot:run
```

**Bash / Zsh**

```bash
AWS_ENDPOINT=http://localhost:4566 ./mvnw spring-boot:run
```

---

## Configuration

| Variable       | Description                                                                 | Example                 |
|----------------|-----------------------------------------------------------------------------|-------------------------|
| `AWS_ENDPOINT` | Overrides the AWS endpoint. Set it for LocalStack, leave unset for real AWS | `http://localhost:4566` |

Dummy credentials (`test` / `test`) and the `us-east-1` region are enough when running against LocalStack.

---

## API Endpoints

| Method | Endpoint                                        | Description                   |
|--------|-------------------------------------------------|-------------------------------|
| POST   | `/api/documents/upload`                         | Upload a file to S3           |
| GET    | `/api/documents/download/**`                    | Stream download a file by key |
| GET    | `/api/documents/presigned-download-url?key=`    | Get presigned download URL    |
| GET    | `/api/documents/presigned-upload-url?filename=` | Get presigned upload URL      |

---

## Quick Test

```bash
# Upload a file (the multipart field name is assumed to be "file"; check DocumentController)
curl -F "file=@./sample.pdf" http://localhost:8080/api/documents/upload

# Get a presigned download URL
curl "http://localhost:8080/api/documents/presigned-download-url?key=<your-key>"

# Check that the event reached the queues
awslocal sqs get-queue-attributes \
  --queue-url http://localhost:4566/000000000000/oms-analytics \
  --attribute-names ApproximateNumberOfMessages
```

After an upload, the application log should show the `DocumentProcessor` report within about 5 seconds.

---

## Key Design Decisions

- **Streaming downloads**: files are served via `StreamingResponseBody` to avoid loading large files into the heap
- **Presigned URLs**: direct client-to-S3 transfers keep Spring Boot out of the data path
- **SNS fan-out**: a single publish triggers all downstream consumers simultaneously
- **At-least-once processing**: messages are not deleted on failure, so SQS retries automatically
- **Poison message handling**: deserialization failures are deleted immediately, infrastructure failures are retried
- **Environment parity**: the same code runs against LocalStack and real AWS, only the endpoint config changes

---

## Troubleshooting

| Problem                                   | Likely cause and fix                                                                        |
|-------------------------------------------|---------------------------------------------------------------------------------------------|
| `Connection refused` on port 4566         | LocalStack is not running. Run `docker compose up -d` and check `docker compose ps`         |
| `NoSuchBucket` / `QueueDoesNotExist`      | The `init/` setup did not run, or the container was recreated. Re-run it or use steps 4 to 6 |
| Consumer logs nothing after upload        | Check that the `oms-documents` queue is subscribed to the SNS topic (step 6)                |
| Presigned URL does not work from outside  | The URL host must be reachable from the client, so check `AWS_ENDPOINT`                     |
| Messages end up in the DLQ                | Processing failed 3 times. Inspect them with `awslocal sqs receive-message` on the DLQ      |

---

## Course Progress

This project is part of a structured AWS for Java Backend Developers course.

```
Module 1  — LocalStack Fundamentals           ✅ Complete
Module 2  — AWS CLI                           ✅ Merged into Module 1
Module 3  — IAM basics for developers         ⏭️ Deferred (needs real AWS)
Module 4  — S3 with Spring Boot               ✅ Complete
Module 5  — SQS                               ✅ Complete
Module 6  — SNS                               ✅ Complete
Module 7  — Event-driven Architecture         ✅ Complete
             Unit 1: Outbox pattern
             Unit 2: Correlation IDs & MDC
────────────────────────────────────────────────────────
Module 8  — DynamoDB                          🔄 Next
Module 9  — RDS with Spring Boot              ➕ Added
Module 10 — Spring Cloud AWS                  ⬜ Pending
Module 11 — Secrets Manager                   ⬜ Pending
Module 12 — Parameter Store                   ⬜ Pending
Module 13 — Lambda for Java developers        ⬜ Pending
Module 14 — API Gateway                       ⬜ Pending
Module 15 — CloudWatch logging & monitoring   ⬜ Pending
Module 16 — Infrastructure basics             ⬜ Pending
Module 17 — Deploying Spring Boot (incl. EC2) ⬜ Pending
Module 18 — Testing AWS integrations          ⬜ Pending
Module 19 — Performance optimization          ⬜ Pending
Module 20 — Final production project          ⬜ Pending
────────────────────────────────────────────────────────
Progress:  6 / 20 modules done  ▓▓▓▓▓▓░░░░░░░░░░░░░░  30%
```