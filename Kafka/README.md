# Kafka Setup for MarineTracker

This setup streams vessel location data from a CSV file (sorted) to a Kafka topic using a python producer.

## How It Works

1. **Kafka Setup**  
   Apache Kafka and Zookeeper are included in the `kafka_2.12-3.9.0/` directory. Zookeeper is used to coordinate Kafka broker. In order for Kafka to run, Zookeeper must be executed first.

2. **Topic Creation**  
   A topic named `location_updates` is used to publish vessel location updates.

3. **Data Source**  
   Not uploaded here because of big size. Available at https://owncloud.skel.iit.demokritos.gr/index.php/s/9EsxBK0Bk4ebudk. The file `dataset/ais/nari_dynamic_sorted.csv` contains AIS vessel data, such as MMSI, location, speed, and timestamp. This is a sorted version of the `nari_dynamic.csv` file, via the `sort.sh` script.

4. **Producer Script**  
   `producer.py` reads the CSV and sends each row as a JSON message to the Kafka topic. It simulates real-time streaming by sleeping between messages based on their timestamp.

5. **Consumer Script**  
   A Kafka console consumer is available to read and display the messages being published to the topic.
   For the project, a "real" Spring Boot Java consumer will be used.

## How to Run

### 1. Start Zookeeper and Kafka

```bash
  cd Kafka/kafka_2.12-3.9.0
  ./start.sh
```

This runs:
- Zookeeper (on port 2181)
- Kafka broker (on port 9092)

### 2. Create the Kafka Topic

```bash
  ./create_topic.sh
```

This creates the location_updates topic if it doesn't already exist (ignore the "_" naming error message).

### 3. Run the Python Producer

```bash
  cd ../dataset
  python3 producer.py
```

Make sure you have the required Python packages (python3 pip, pandas, confluent_kafka, ...).
Also, make sure you have locally the ai/ directory with all its contents (including a sorted version of ai data stream).

### 4. Run the Kafka Console Consumer (Optional)

```bash
  cd ../kafka_2.12-3.9.0
  ./consumer.sh
```

This prints all messages from the location_updates topic (starting from beginning, but this can change).

Notes
- The CSV data directory (dataset/ais/) is not included in the repo due to its size.
- Adjust the CSV path in producer.py if you use a different file.
