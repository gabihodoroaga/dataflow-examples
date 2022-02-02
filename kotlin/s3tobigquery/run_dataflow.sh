#!/bin/bash

export PROJECT_ID="${PROJECT_ID:-demo-project-123}"
export REGION="${REGION:-us-central1}"
export BUCKET_NAME="${BUCKET_NAME:-demo-project-123-storage}"
export AWS_SOURCE_BUCKET="${AWS_SOURCE_BUCKET:-s3://dataflow-example}"
export AWS_REGION="${AWS_REGION:-us-west-2}"
export BIGQUERY_DATASET="${BIGQUERY_DATASET:-demo_bq_dataset}"
export BIGQUERY_TABLE="${BIGQUERY_TABLE:-demo_bq_table_s3}"

# --awsCredentialsProvider='{\"@type\":\"AWSStaticCredentialsProvider\",\"awsAccessKeyId\":\"AKIASHY3R7TEVXCGQWTB\",\"awsSecretKey\":\"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\"}' \


gradle run --args="--runner=DataflowRunner \
    --project=$PROJECT_ID \
    --region=$REGION \
    --workerMachineType=e2-medium \
    --maxNumWorkers=1 \
    --stagingLocation=gs://$BUCKET_NAME/staging/demo-test \
    --tempLocation=gs://$BUCKET_NAME/temp/demo-test \
    --templateLocation=gs://$BUCKET_NAME/demo-test-template.json \
    --awsRegion=$AWS_REGION \
    --sourceBucket=$AWS_SOURCE_BUCKET \
    --outputTable=$BIGQUERY_DATASET.$BIGQUERY_TABLE"

JOB_NAME=demo-test-`date -u +"%Y%m%d-%H%M%S"`

gcloud dataflow jobs run ${JOB_NAME} \
    --gcs-location=gs://$BUCKET_NAME/demo-test-template.json \
    --region=$REGION \
    --parameters="startDate=2022-01-02T03:00:00Z,lookBack=6"
