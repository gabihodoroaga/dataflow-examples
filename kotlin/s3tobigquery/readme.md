# S3 to BgQuery

This is an complex example of how batch import date from S3 into BigQuery using
Apache Beam and DataFlow. You can find posts related to this repo at [hodo.dev](https://hodo.dev/tags/dataflow/)

## Features

- Read the AWS credential from GCP Secret Manager
- The file processing is idempotent. You can safely run the pipeline multiple times without generation duplicates.

## What the pipeline does

1. Generate list of folders taking into consideration the current date and the number of hours to look back
1. Validate if the folders actually exists
1. For each folder get the list of the containing files
1. Filter out all the files that have a corresponding ".parsed" file
1. Decompress the file and process the content as csv
1. Write to BigQuery
1. Write the ".parsed" file back to the bucket

## How ro run

Run locally:

```bash
./run_local.sh 
```

Run on GCP Dataflow:

```bash
./run_dataflow.sh
```

## Author

Gabriel Hodoroaga [hodo.dev](https://hodo.dev)
