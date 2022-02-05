# geolocation

This project is an example of how to find the country, region and the city from latitude and longitude
while streaming data from Pub/Sub to BigQuery using Dataflow and Apache Beam. You can find more by
reading this blog post [hodo.dev](https://hodo.dev/posts/post-36-gcp-dataflow-geolocation/)

## How to setup and run

### Setup 

Create the Pub/Sub topic and subscription 

```bash
PROJECT_ID=`gcloud config list --format 'value(core.project)' 2>/dev/null`

gcloud pubsub topics create demo-topic --project=$PROJECT_ID

gcloud pubsub gcloud pubsub subscriptions create demo-topic-sub --topic=demo-topic --project=$PROJECT_ID
```

Push sample data

```bash
PROJECT_ID=`gcloud config list --format 'value(core.project)' 2>/dev/null`
TODPIC_ID=demo-topic
python3 testdata/push_to_pubsub.py testdata/message.json
```

### Run locally

```bash
./run_local.sh 
```

### Run on Dataflow

```bash
./run_dataflow.sh
```

## Author

Gabriel Hodoroaga ([hodo.dev](https://hodo.dev)).
