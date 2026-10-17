# AWS Document Training

A production-style Spring Boot application built as part of a structured AWS learning course for Java backend developers.

The project integrates with AWS services using **AWS SDK v2**, developed and tested locally with **LocalStack** — no real AWS account required.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 |
| AWS SDK | AWS SDK v2 (2.25.40) |
| Local AWS | LocalStack |
| Containerization | Docker / Docker Compose |
| Build | Maven |
| Utilities | Lombok, Jackson |

---

## AWS Services Covered

| Service | Status |
|---|---|
| S3 — Upload, Download, Streaming, Presigned URLs | ✅ Complete |
| SQS — Async messaging, DLQ, Long polling | ✅ Complete |
| SNS — Pub/Sub, Fan-out pattern, Envelope unwrapping | ✅ Complete |
| DynamoDB | 🔄 Upcoming |
| Secrets Manager | 🔄 Upcoming |
| Parameter Store | 🔄 Upcoming |
| Lambda | 🔄 Upcoming |
| CloudWatch | 🔄 Upcoming |

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
                                 (@Scheduled — polls every 5s)
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
- After upload, event published to SNS topic `oms-document-uploaded`
- SNS fans out to three SQS queues simultaneously
- Background consumer polls `oms-documents` queue every 5 seconds
- SNS envelope unwrapped automatically before processing
- Failed messages retry up to 3 times, then moved to Dead Letter Queue
- Poison messages (deserialization failures) deleted immediately

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

### 2. Configure dummy AWS credentials

```bash
aws configure
# Access Key ID:     test
# Secret Access Key: test
# Region:            us-east-1
# Output format:     json
```

### 3. Set up `awslocal` alias (PowerShell)

```powershell
function awslocal { aws --endpoint-url http://localhost:4566 $args }
```

### 4. Create S3 buckets

```powershell
awslocal s3 mb s3://oms-documents
awslocal s3 mb s3://oms-reports
```

### 5. Create SQS queues

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

### 6. Create SNS topic and subscribe queues

```powershell
awslocal sns create-topic --name oms-document-uploaded

awslocal sns subscribe `
  --topic-arn arn:aws:sns:us-east-1:000000000000:oms-document-uploaded `
  --protocol sqs `
  --notification-endpoint arn:aws:sqs:us-east-1:000000000000:oms-documents

awslocal sns subscribe `
  --topic-arn arn:aws:sns:us-east-1:000000000000:oms-document-uploaded `
  --protocol sqs `
  --notification-endpoint arn:aws:sqs:us-east-1:000000000000:oms-email-notifications

awslocal sns subscribe `
  --topic-arn arn:aws:sns:us-east-1:000000000000:oms-document-uploaded `
  --protocol sqs `
  --notification-endpoint arn:aws:sqs:us-east-1:000000000000:oms-analytics
```

### 7. Run the application

```powershell
$env:AWS_ENDPOINT="http://localhost:4566"
./mvnw spring-boot:run
```

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/documents/upload` | Upload a file to S3 |
| GET | `/api/documents/download/**` | Stream download a file by key |
| GET | `/api/documents/presigned-download-url?key=` | Get presigned download URL |
| GET | `/api/documents/presigned-upload-url?filename=` | Get presigned upload URL |

---

## Key Design Decisions

- **Streaming downloads** — files served via `StreamingResponseBody` to avoid loading large files into heap
- **Presigned URLs** — direct client-to-S3 transfers, Spring Boot stays out of the data path
- **SNS fan-out** — single publish triggers all downstream consumers simultaneously
- **At-least-once processing** — messages not deleted on failure, SQS retries automatically
- **Poison message handling** — deserialization failures deleted immediately, infrastructure failures retried
- **Environment parity** — same code runs against LocalStack and real AWS, only endpoint config changes

---

## Course Progress

This project is part of a structured AWS for Java Backend Developers course.

```
✅ Module 1  — LocalStack Fundamentals
✅ Module 2  — S3 with Spring Boot
✅ Module 3  — SQS
✅ Module 4  — SNS & Event-driven Architecture
🔄 Module 5  — DynamoDB (upcoming)
⬜ Module 6  — Secrets Manager & Parameter Store
⬜ Module 7  — Lambda for Java Developers
⬜ Module 8  — CloudWatch
⬜ Module 9  — Deploying Spring Boot to AWS
⬜ Module 10 — Final Production Project
```