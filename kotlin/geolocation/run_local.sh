#!/bin/bash

export PROJECT_ID="${PROJECT_ID:-demo-project-123}"
export REGION="${REGION:-us-central1}"
export SUBSCRIPTION_ID="${SUBSCRIPTION_ID:-projects/demo-project-123/subscriptions/demo-topic-sub}"
export BIGQUERY_DATASET="${BIGQUERY_DATASET:-demo_bq_dataset}"
export BIGQUERY_TABLE="${BIGQUERY_TABLE:-geo_bq_table}"

gradle run --args=" \
    --runner=DirectRunner \
    --project=$PROJECT_ID \
    --region=$REGION \
    --inputSubscription=$SUBSCRIPTION_ID \
    --outputTable=$BIGQUERY_DATASET.$BIGQUERY_TABLE"
