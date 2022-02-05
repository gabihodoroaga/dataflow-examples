import os
import json
import sys

from google.cloud import pubsub_v1

project_id = os.getenv("PROJECT_ID")
topic_id = os.getenv("TOPIC_ID")

publisher = pubsub_v1.PublisherClient()
topic_path = publisher.topic_path(project_id, topic_id)

def print_usage():
    print("Usage: python3 push_to_pubsub.py [options] file")
    print()
    print("Options:")
    print("-m       : multiline - send a message for each line.")


if len(sys.argv) == 2:
    file_path = sys.argv[1]
    if not file_path.endswith(".json"):
        file_path += ".json"

    data = json.load(open(file_path))
    future = publisher.publish(
        topic_path, json.dumps(data).encode("utf-8"), origin="python-sample"
    )
    print(file_path, future.result())
elif len(sys.argv) == 3: 
    if sys.argv[1] != "-m":
        print_usage()
        exit(1)
    
    file_path = sys.argv[2]
    if not file_path.endswith(".json"):
        file_path += ".json"

    count = 0
    file = open(file_path, 'r')
    for line in file:
        count += 1
        future = publisher.publish(
            topic_path, line.encode("utf-8"), origin="python-sample"
        )
        print(f"message sent {count}, result: {future.result()}", end='\r')
    file.close()
    print("\nDone")
else:
    print_usage()
    exit(1)
