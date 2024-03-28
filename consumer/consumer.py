import os
import pika
import sys
import logging


def get_consumer_name():
    consumer_name = os.getenv("CONSUMER_NAME", "").strip()
    if len(consumer_name) < 3:
        consumer_name = "consumer"
    return consumer_name


def main():
    host = os.getenv("RABBITMQ_HOST", "localhost")
    exchange = os.getenv("RABBITMQ_EXCHANGE", "logs")
    consumer_name = get_consumer_name()

    conn = pika.BlockingConnection(pika.ConnectionParameters(host))
    channel = conn.channel()

    channel.exchange_declare(exchange, exchange_type="fanout")
    result = channel.queue_declare("", exclusive=True)
    queue = result.method.queue

    channel.queue_bind(queue, exchange)

    def on_message_callback(ch, method, properties, body: bytes):
        del ch, method, properties
        logging.warning(f" [x] {consumer_name}: Received {body.decode()!r}")

    channel.basic_consume(queue, on_message_callback, auto_ack=True)

    logging.warning(" [*] Waiting for logs. To exit press CTRL+C")
    channel.start_consuming()


if __name__ == "__main__":
    logging.basicConfig(level=logging.WARN)

    try:
        main()
    except Exception as e:
        logging.error(str(e))
        sys.exit(1)
    except KeyboardInterrupt:
        logging.warning("Interrupted")
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)
