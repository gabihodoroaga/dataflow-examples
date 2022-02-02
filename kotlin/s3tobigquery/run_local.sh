#!/bin/bash

export PROJECT_ID="${PROJECT_ID:-demo-project-123}"
export REGION="${REGION:-us-central1}"
export BUCKET_NAME="${BUCKET_NAME:-demo-project-123-storage}"
export AWS_SOURCE_BUCKET="${AWS_SOURCE_BUCKET:-s3://dataflow-example}"
export AWS_REGION="${AWS_REGION:-us-west-2}"
export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:-secret://}"
export AWS_SECRET_KEY="${AWS_SECRET_KEY:-demoSecretKey}" 
export BIGQUERY_DATASET="${BIGQUERY_DATASET:-demo_bq_dataset}"
export BIGQUERY_TABLE="${BIGQUERY_TABLE:-demo_bq_table_s3}"

gradle run --args="--runner=DirectRunner \
    --project=$PROJECT_ID \
    --region=$REGION \
    --tempLocation=gs://$BUCKET_NAME/temp/dataflow-demo \
    --awsRegion=$AWS_REGION \
    --sourceBucket=$AWS_SOURCE_BUCKET \
    --awsAccessKeyId=\"$AWS_ACCESS_KEY_ID\" \
    --awsSecretKey=\"$AWS_SECRET_KEY\" \
    --startDate=2022-01-02T03:00:00Z \
    --lookBack=6 \
    --outputTable=$BIGQUERY_DATASET.$BIGQUERY_TABLE"

#--awsCredentialsProvider='{\"@type\":\"AWSStaticCredentialsProvider\",\"awsAccessKeyId\":\"$AWS_ACCESS_KEY_ID\",\"awsSecretKey\":\"$AWS_SECRET_KEY\"}' \
