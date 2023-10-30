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


