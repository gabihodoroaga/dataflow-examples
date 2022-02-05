#!/bin/bash

export PROJECT_ID="${PROJECT_ID:-demo-project-123}"
export REGION="${REGION:-us-central1}"
export SUBSCRIPTION_ID="${SUBSCRIPTION_ID:-projects/demo-project-123/subscriptions/demo-topic-sub}"
export BIGQUERY_DATASET="${BIGQUERY_DATASET:-demo_bq_dataset}"
export BIGQUERY_TABLE="${BIGQUERY_TABLE:-demo_bq_table}"
export BUCKET_NAME="${BUCKET_NAME:-demo-project-123-storage}"

gradle run --args=" \
    --runner=DataflowRunner \
    --project=$PROJECT_ID \
    --region=$REGION \
    --workerMachineType=e2-medium \
    --maxNumWorkers=1 \
    --enableStreamingEngine \
    --stagingLocation=gs://$BUCKET_NAME/staging/demo-test \
    --tempLocation=gs://$BUCKET_NAME/temp/demo-test \
    --templateLocation=gs://$BUCKET_NAME/demo-test-template.json \
    --inputSubscription=$SUBSCRIPTION_ID \
    --outputTable=$BIGQUERY_DATASET.$BIGQUERY_TABLE"

JOB_NAME=demo-test-`date -u +"%Y%m%d-%H%M%S"`

gcloud dataflow jobs run ${JOB_NAME} \
    --gcs-location=gs://$BUCKET_NAME/demo-test-template.json \
    --region=$REGION

