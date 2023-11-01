# Kafka Spring Boot

Sample Application for Kafka and Spring Boot.

## Install Kafka (locally)

First things first. For a Quickstart tutorial on how to do a Kafka installation 
see [APACHE KAFKA QUICKSTART](https://kafka.apache.org/quickstart) at Kafka.org.

Download the latest Kafka release and extract it.

```bash
$ tar -xzf kafka_2.13-3.6.0.tgz
$ cd kafka_2.13-3.6.0
```

Apache Kafka can be started using ZooKeeper or KRaft.

## Start Kafka with ZooKeeper

Run the following commands in order to start all services in the correct order.

```bash
# Start the ZooKeeper service
$ bin/zookeeper-server-start.sh config/zookeeper.properties
```

Open another terminal session - on Mac use `Cmd-Shift D` - and run.

```bash
# Start the Kafka broker service
$ bin/kafka-server-start.sh config/server.properties
```

Once all services have successfully launched, you will have a basic Kafka environment running and ready to use.

## Send and receive a message on the command line

In order to test Kafka we can open another terminal and start a consumer that listens to `topic1`.

```bash
bin/kafka-console-consumer.sh --topic topic1 --from-beginning --bootstrap-server localhost:9092
```

Now we can test if kafka works. Open a new terminal and start a producer

```bash
bin/kafka-console-producer.sh --topic topic1 --bootstrap-server localhost:9092
```

Now we can enter a text.

```bash
~/dev/kafka/kafka_2.13-3.6.0  bin/kafka-console-producer.sh --topic topic1 --bootstrap-server localhost:9092
>Hello World!
>
```

## Kafka Auto-Configuration in Spring Boot

Spring provides auto-configuration support for Kafka.
Kafka configuration is controlled configuration properties that start with `spring.kafka.*`.
For example, you might declare the following properties in our `application.properties`.

```bash
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=myGroup
```

## Create a Consumer in Spring Boot

Now we create a small [consumer app](https://docs.spring.io/spring-kafka/docs/current/reference/html/#spring-boot-consumer-app).
In order to create a topic on startup, we add a bean of type NewTopic.
If the topic already exists, the bean is ignored.

```java
package io.eyce.sample.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication
public class KafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args);
	}

	@Bean
	public NewTopic topic() {
		return TopicBuilder.name("topic1")
				.partitions(10)
				.replicas(1)
				.build();
	}

	@KafkaListener(id = "myId", topics = "topic1")
	public void listen(String in) {
		System.out.println(in);
	}
}
```

## Create a Producer in Spring Boot

Spring’s `KafkaTemplate` is auto-configured, and you can auto-wire it directly in your own beans.
We can use the `template` to send a message to our `topic1`.

```java
package io.eyce.sample.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class KafkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaApplication.class, args);
    }

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name("topic1")
                .partitions(10)
                .replicas(1)
                .build();
    }

    @KafkaListener(id = "myId", topics = "topic1")
    public void listen(String in) {
        System.out.println(in);
    }

    @Bean
    public ApplicationRunner runner(KafkaTemplate<String, String> template) {
        return args -> template.send("topic1", "Hello World from Spring Boot!");
    }
}
```

## Move the Config into its own Configuration Class

We can move the Configuration into its own class.

```java
package io.eyce.sample.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name("topic1")
                .partitions(10)
                .replicas(1)
                .build();
    }
}
```

## Add Kafka Configuration for Producer and Consumer

Now we can add Producer and Consumer Configuration using our properties `producerProps` and `consumerProps`.

```java
package io.eyce.sample.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name("topic1")
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerProps());
    }

    @Bean
    public Map<String, Object> producerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // See https://kafka.apache.org/documentation/#producerconfigs for more properties
        return props;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<String, String>(producerFactory());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory(ConsumerFactory<Integer, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<Integer, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerProps());
    }

    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        // props.put(ConsumerConfig.GROUP_ID_CONFIG, "group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}

```

## Terminate the Kafka Environment

Now that you reached the end of the quickstart, feel free to tear down the Kafka environment—or continue playing around.

- Stop the producer and consumer clients with Ctrl-C, if you haven't done so already.
- Stop the Kafka broker with Ctrl-C.
- Lastly, if the Kafka with ZooKeeper section was followed, stop the ZooKeeper server with Ctrl-C.

If you also want to delete any data of your local Kafka environment including any events
you have created along the way, run the following command.

```bash
rm -rf /tmp/kafka-logs /tmp/zookeeper /tmp/kraft-combined-logs
```


