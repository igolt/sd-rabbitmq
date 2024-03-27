import os
import pika
import sys
import logging


def main():
    host = os.getenv("RABBITMQ_HOST", "localhost")
    queue_name = os.getenv("RABBITMQ_QUEUE", "hello")

    conn = pika.BlockingConnection(pika.ConnectionParameters(host=host))
    channel = conn.channel()

    channel.queue_declare(queue_name)

    def on_message_cb(ch, method, properties, body):
        del ch, method, properties
        logging.warning(f" [x] Received {body}")

    channel.basic_consume(
        queue=queue_name, on_message_callback=on_message_cb, auto_ack=True
    )

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
