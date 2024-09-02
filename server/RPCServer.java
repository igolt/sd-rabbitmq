import com.rabbitmq.client.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPCServer {
    public static final String DEFAULT_HOST = "localhost";
    public static final String QUEUE_NAME = "rpc_queue";

    public static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);

        return value == null ? defaultValue : value;
    }

    public static int fib(int n) {
        if (n == 0 || n == 1) {
            return n;
        }
        int prev = 1, prevprev = 0, value = 1;

        for (int i = 1; i < n; i++) {
            value = prev + prevprev;
            prevprev = prev;
            prev = value;
        }
        return value;
    }

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        Logger logger = LoggerFactory.getLogger(RPCServer.class);


        factory.setHost(getEnv("RABBITMQ_HOST", DEFAULT_HOST));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // channel.queuePurge(QUEUE_NAME);

        channel.basicQos(1);

        logger.info(" [x] Awaiting RPC requests");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();

            String response = "";
            try {
                String message = new String(delivery.getBody(), "UTF-8");
                int n = Integer.parseInt(message);

                response += fib(n);
                logger.info(" [.] fib(" + message + ") = " + response);
            } catch (RuntimeException e) {
                logger.error(" [.] " + e);
            } finally {
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
        };

        channel.basicConsume(QUEUE_NAME, false, deliverCallback, (consumerTag -> {}));
    }
}
