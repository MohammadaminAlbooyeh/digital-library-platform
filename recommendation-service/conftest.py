import os
import sys

# Disable the Kafka consumer during tests: with no bootstrap servers the
# startup hook skips connecting to a broker that isn't there.
os.environ["KAFKA_BOOTSTRAP_SERVERS"] = ""

sys.path.insert(0, os.path.dirname(__file__))
