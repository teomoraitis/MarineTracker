import time
import json
import pandas as pd
from confluent_kafka import Producer

KAFKA_BROKER = "localhost:9092"  
TOPIC = "location_updates"

# Create Kafka producer
producer = Producer({'bootstrap.servers': KAFKA_BROKER})

# Load CSV file
CSV_FILE = "./ais/nari_dynamic_sorted.csv" 
df = pd.read_csv(CSV_FILE)

def safe_value(value, default):
    return default if pd.isna(value) else value

def send_ship_data():
    prev_time = -1
    for _, row in df.iterrows():
        # Create a JSON message from CSV row
        #sourcemmsi,navigationalstatus,rateofturn,speedoverground,courseoverground,trueheading,lon,lat,t
        message = {
            "mmsi": int(row["sourcemmsi"]),
            "status": int(safe_value(row.get("navigationalstatus"), -1)),  # Default status: -1 (unknown)
            "turn": float(safe_value(row.get("rateofturn"), 0.0)),  # Default turn: 0 (no turn)
            "speed": float(safe_value(row.get("speedoverground"), 0.0)),  # Default speed: 0 knots
            "course": float(safe_value(row.get("courseoverground"), 0.0)),  # Default course: 0 degrees
            "heading": int(safe_value(row.get("trueheading"), 0)),  # Default heading: 0 degrees
            "lon": float(row["lon"]),
            "lat": float(row["lat"]),
            "timestamp": int(row["t"])  # UNIX timestamp
        }

        if (prev_time < 0):
            # Send message to Kafka topic
            producer.produce(TOPIC, value=json.dumps(message))
            prev_time = message['timestamp']
        else:
            difference = message['timestamp'] - prev_time
            time.sleep(difference) 
            producer.produce(TOPIC, value=json.dumps(message))
            prev_time = message['timestamp']
        
        # Flush to ensure messages are sent
        print(f"Vessel Sent: {message}")
        producer.flush()

if __name__ == "__main__":
    print("Kafka CSV Ship Producer Started...")
    send_ship_data()


