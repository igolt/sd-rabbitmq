import logging
import os
import pika
import sys
import time


def main():
    host = os.getenv("RABBITMQ_HOST", "localhost")
    queue = os.getenv("RABBITMQ_QUEUE", "task_queue")

    conn = pika.BlockingConnection(pika.ConnectionParameters(host=host))
    channel = conn.channel()

    channel.queue_declare(queue, durable=True)

    def on_message_callback(ch, method, properties, body: bytes):
        del properties
        logging.warning(f" [x] Received {body.decode()}")
        time.sleep(body.count(b"."))
        logging.warning(" [x] Done\n")
        ch.basic_ack(delivery_tag=method.delivery_tag)

    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue, on_message_callback)

    logging.warning(" [*] Waiting for messages. To exit press CTRL+C")
    channel.start_consuming()


if __name__ == "__main__":
    logging.basicConfig(level=logging.WARN)

    try:
        main()
    except KeyboardInterrupt:
        logging.warning("Interrupted")
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)
