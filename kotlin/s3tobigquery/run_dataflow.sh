#!/bin/bash

export PROJECT_ID="${PROJECT_ID:-demo-project-123}"
export REGION="${REGION:-us-central1}"
export BUCKET_NAME="${BUCKET_NAME:-demo-project-123-storage}"
export AWS_SOURCE_BUCKET="${AWS_SOURCE_BUCKET:-s3://dataflow-example}"
export AWS_REGION="${AWS_REGION:-us-west-2}"
export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:-secret:projects/123456789/secrets/aws_access_key_id/versions/latets}"
export AWS_SECRET_KEY="${AWS_SECRET_KEY:-secret:projects/75641163784/secrets/aws_secret_key/versions/latest}" 
export BIGQUERY_DATASET="${BIGQUERY_DATASET:-demo_bq_dataset}"
export BIGQUERY_TABLE="${BIGQUERY_TABLE:-demo_bq_table_s3}"


gradle run --args="--runner=DataflowRunner \
    --project=$PROJECT_ID \
    --region=$REGION \
    --workerMachineType=e2-medium \
    --maxNumWorkers=1 \
    --stagingLocation=gs://$BUCKET_NAME/staging/demo-test \
    --tempLocation=gs://$BUCKET_NAME/temp/demo-test \
    --templateLocation=gs://$BUCKET_NAME/demo-test-template.json \
    --awsAccessKeyId=\"$AWS_ACCESS_KEY_ID\" \
    --awsSecretKey=\"$AWS_SECRET_KEY\" \
    --awsRegion=$AWS_REGION \
    --sourceBucket=$AWS_SOURCE_BUCKET \
    --outputTable=$BIGQUERY_DATASET.$BIGQUERY_TABLE"

# --awsCredentialsProvider='{\"@type\":\"AWSStaticCredentialsProvider\",\"awsAccessKeyId\":\"$AWS_ACCESS_KEY_ID\",\"awsSecretKey\":\"$AWS_SECRET_KEY\"}' \

JOB_NAME=demo-test-`date -u +"%Y%m%d-%H%M%S"`

gcloud dataflow jobs run ${JOB_NAME} \
    --gcs-location=gs://$BUCKET_NAME/demo-test-template.json \
    --region=$REGION \
    --parameters="startDate=2022-01-02T03:00:00Z,lookBack=6"
