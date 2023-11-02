package io.eyce.sample.kafka.domain;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    private String title;
    private String author;
}
