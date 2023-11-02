package io.eyce.sample.kafka;

import io.eyce.sample.kafka.domain.Book;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class KafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args);
	}

	@KafkaListener(id = "myId", topics = "books")
	public void listen(Book b) {
		System.out.println(b);
	}

	@Bean
	public ApplicationRunner runner(KafkaTemplate<String, Book> template) {
		return args -> {
			template.send("books", new Book("Kafka in Action", "Dylan Scott, Viktor Gamov, Dave Klein"));
		};
	}
}
