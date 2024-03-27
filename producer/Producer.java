import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Producer {
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_QUEUE_NAME = "hello";

    public static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);

        return value == null ? defaultValue : value;
    }

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        Logger logger = LoggerFactory.getLogger(Producer.class);
        String queueName, host;

        host = getEnv("RABBITMQ_HOST", DEFAULT_HOST);
        queueName = getEnv("RABBITMQ_QUEUE", DEFAULT_QUEUE_NAME);

        factory.setHost(host);
        try (Connection conn = factory.newConnection();
             Channel channel = conn.createChannel()) {
            channel.queueDeclare(queueName, false, false, false, null);
            String message = "Hello World!";

            while (true) {
                channel.basicPublish("", queueName, null, message.getBytes());
                logger.info(" [x] Sent '" + message + "'");
                Thread.sleep(10000);
            }
        }
    }
}
