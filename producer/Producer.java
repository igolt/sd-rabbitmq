import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class Producer {
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_QUEUE_NAME = "task_queue";

    public static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);

        return value == null ? defaultValue : value;
    }

    public static String getDotsStr(int n) {
        String dots = "";

        for (int i = 0; i < n; i++) {
            dots += ".";
        }
        return dots;
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
            boolean durable = true;
            channel.queueDeclare(queueName, durable, false, false, null);

            Random random = new Random();
            int messageNumber = 1;
            while (true) {
                String message = String.format(
                    "Message %d%s",
                    messageNumber++,
                    getDotsStr(random.nextInt(13) + 1)
                );
                channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
                logger.info(" [x] Sent '" + message + "'");
                Thread.sleep(5000);
            }
        }
    }
}
