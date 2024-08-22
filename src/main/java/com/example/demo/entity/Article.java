package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity @Table(
    indexes = @Index(name = "idx_article_publish_at", columnList = "published_at")
)
@NoArgsConstructor @AllArgsConstructor
public class Article {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public long id;
    @Column(nullable = false, length = 100) public String title;
    @Column(nullable = false) public String author;
    // NB: clob???
    @Column(nullable = false, length = 9000) public String content;
    @Column(nullable = false) public LocalDate publishedAt;
}
