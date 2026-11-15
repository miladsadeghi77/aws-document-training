CREATE TABLE documents
(
    id                UUID                     NOT NULL,
    s3_key            VARCHAR(255)             NOT NULL,
    bucket            VARCHAR(255)             NOT NULL,
    original_filename VARCHAR(255)             NOT NULL,
    status            VARCHAR(255)             NOT NULL,
    uploaded_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_documents PRIMARY KEY (id)
);

CREATE TABLE outbox_events
(
    id              UUID                     NOT NULL,
    event_type      VARCHAR(255)             NOT NULL,
    payload         TEXT                     NOT NULL,
    status          VARCHAR(255)             NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at    TIMESTAMP WITH TIME ZONE,
    retry_count     INTEGER                  NOT NULL DEFAULT 0,
    last_retried_at TIMESTAMP WITH TIME ZONE,
    correlation_id  VARCHAR(255),
    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);