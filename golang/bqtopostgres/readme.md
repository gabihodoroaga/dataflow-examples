# bqtopostgres

This is an example of how you can transfer data from BigQuery to Postgres using a Dataflow pipeline.

## How to build and run

Build

```bash
env CGO_ENABLED=0 go build -o bqtopostgres
```

Run locally

```bash
export PROJECT_ID="${PROJECT_ID:-demo-project-123}"
export REGION="${REGION:-us-central1}"
export BUCKET_NAME="${BUCKET_NAME:-demo-project-123-storage}"
export POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
export POSTGRES_PORT="${POSTGRES_PORT:-5432}"
export POSTGRES_USER="${POSTGRES_USER:-postgres}"
export POSTGRES_PASS="${POSTGRES_HOST:-password}"

./bqtopostgres  \
    --project $PROJECT_ID \
    --region=$REGION \
    --pgconn="postgres://$POSTGRES_USER:$POSTGRES_PASS@$POSTGRES_HOST:$POSTGRES_PORT/bqdemo?sslmode=disable"
```

Run using Dataflow

```bash
export PROJECT_ID="${PROJECT_ID:-demo-project-123}"
export REGION="${REGION:-us-central1}"
export BUCKET_NAME="${BUCKET_NAME:-demo-project-123-storage}"
export POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
export POSTGRES_PORT="${POSTGRES_PORT:-5432}"
export POSTGRES_USER="${POSTGRES_USER:-postgres}"
export POSTGRES_PASS="${POSTGRES_HOST:-password}"

JOB_NAME=bqdemo-test-`date -u +"%Y%m%d-%H%M%S"`

env CGO_ENABLED=0 ./bqtopostgres  \
            --runner dataflow \
            --execute_async \
            --job_name $JOB_NAME \
            --project $PROJECT_ID \
            --region=$REGION \
            --worker_machine_type=e2-medium \
            --max_num_workers=1 \
            --temp_location gs://$BUCKET_NAME/temp/bqdemo \
            --staging_location gs://$BUCKET_NAME/staging/bqdemo \
            --worker_harness_container_image=apache/beam_go_sdk:latest \
            --pgconn=postgres://$POSTGRES_USER:$POSTGRES_PASS@$POSTGRES_HOST:$POSTGRES_PORT/bqdemo?sslmode=disable
```
