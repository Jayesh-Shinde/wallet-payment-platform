# Wallet Payment Platform

A high-performance, event-driven Wallet Payment Platform built with Spring Boot, PostgreSQL, and Apache Kafka.

## Tech Stack

* **Language & Framework:** Java 25, Spring Boot 4.1.0 (Virtual Threads enabled)
* **Database & Migrations:** PostgreSQL 16, Flyway
* **Messaging & Event Streaming:** Apache Kafka (KRaft mode), Spring Kafka
* **Utilities & Tools:** Lombok, MapStruct, Docker & Docker Compose

## Core Components Summary

* **REST API (`TransferController`):** Exposes HTTP endpoints (`POST /api/transfer`) to accept incoming money transfer requests and capture idempotency headers.
* **Transfer Orchestrator (`TransferOrchestrator`):** Coordinates end-to-end transfer execution, managing key locking and returning cached responses for replayed requests.
* **Idempotency Service (`IdempotencyService`):** Ensures duplicate requests with the same key execute exactly once by storing operation states and results.
* **Transfer Service (`TransferServiceImpl` & `TransferRetryFacade`):** Handles core ledger balances (debit/credit logic) and transaction retries inside database boundaries.
* **Event Relayer (`EventTransferRelayer`):** A scheduled background task implementing the Outbox Pattern to poll database events and publish them to Kafka topics.
* **Kafka Consumer (`TransferEventConsumer`):** Consumes `transfer_complete` Kafka messages to record processed notification events and handle duplicate events safely.

## Prerequisites

* **Java:** JDK 25+
* **Build Tool:** Maven (or `./mvnw` wrapper)
* **Containers:** Docker & Docker Compose

## Getting Started

### 1. Start Infrastructure Services

Spin up PostgreSQL, Kafka, and Kafka UI using Docker Compose:

```bash
docker-compose up -d
```

Services started:
* **PostgreSQL:** `localhost:5432`
* **Kafka Broker:** `localhost:9094`
* **Kafka UI:** `http://localhost:3002`

### 2. Environment Setup

Ensure environment variables exist (or create `.env`):

```env
DB_USER=root
DB_PASSWORD=root
```

### 3. Run the Application

Start the Spring Boot service:

```bash
./mvnw spring-boot:run
```

## API Highlights

* **`POST /api/transfer`**: Initiates a wallet money transfer idempotently using the `X-Idempotency-Key` header.

## Build & Test

```bash
./mvnw clean test
```
