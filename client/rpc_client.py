import time
import pika
import os
from uuid import uuid4
import logging
from random import randint


def get_host() -> str:
    return os.getenv("RABBITMQ_HOST", "localhost")


class FibonacciRpcClient:
    def __init__(self) -> None:
        self.connection = pika.BlockingConnection(pika.ConnectionParameters(get_host()))
        self.channel = self.connection.channel()

        result = self.channel.queue_declare(queue="", exclusive=True)
        self.callback_queue = result.method.queue

        self.channel.basic_consume(
            queue=self.callback_queue,
            on_message_callback=self.on_response,
            auto_ack=True,
        )

        self.response = None
        self.corr_id = None

    def on_response(self, ch, method, props, body) -> None:
        if self.corr_id == props.correlation_id:
            self.response = body

    def call(self, n) -> int:
        self.response = None
        self.corr_id = str(uuid4())
        self.channel.basic_publish(
            exchange="",
            routing_key="rpc_queue",
            properties=pika.BasicProperties(
                reply_to=self.callback_queue, correlation_id=self.corr_id
            ),
            body=str(n),
        )
        while self.response is None:
            self.connection.process_data_events(time_limit=None)
        return int(self.response)


if __name__ == "__main__":
    fib_rpc_client = FibonacciRpcClient()
    while True:
        n = randint(1, 15)
        logging.warning(f" [x] Requesting fib({n})")
        response = fib_rpc_client.call(n)
        logging.warning(f" [.] Got {response}")
        time.sleep(5)
