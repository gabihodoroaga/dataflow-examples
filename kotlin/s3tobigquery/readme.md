# S3 to BgQuery

This is an complex example of how batch import date from S3 into BigQuery using
Apache Beam and DataFlow.

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


AWS Console 
- create a new user `dataflow_user`
- save the key and secret
- create a new bucket `dataflow-example`

- add bucket policy 

```json

```

Crate a .boto file with this conects

```txt
[Credentials]
aws_access_key_id = [AWS_ACCESS_KEY_ID]
aws_secret_access_key = [AWS_SECRET_ACCESS_KEY]
```

Upload sample files to the bucket

```bash
BOTO_CONFIG=.boto gsutil cp -r 2022 s3://dataflow-example/ 
```


1 643 809 249 000 000 000



```bash
./run_local.sh
```
