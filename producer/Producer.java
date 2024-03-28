import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Producer {
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_RABBITMQ_EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(getHost());
        try (
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
        ) {
            Logger logger = LoggerFactory.getLogger(Producer.class);
            String exchangeName = getExchangeName();
            int logNumber = 0;

            channel.exchangeDeclare(exchangeName, "fanout");
            while (true) {
                String msg = "Log " + (++logNumber);

                channel.basicPublish(exchangeName, "", null, msg.getBytes());
                logger.info(" [x] Sent '" + msg + "'");
                Thread.sleep(5000);
            }
        }
    }

    public static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null ? defaultValue : value;
    }

    public static String getHost() {
        return getEnv("RABBITMQ_HOST", DEFAULT_HOST);
    }

    public static String getExchangeName() {
        return getEnv("RABBITMQ_EXCHANGE_NAME", DEFAULT_RABBITMQ_EXCHANGE_NAME);
    }
}
