package com.example.demo.dao;

import com.example.demo.entity.Article;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.List;

public interface ArticlesDao extends JpaRepository<Article, Long> {
    @Query("select a from Article a where (a.id > :lastId or :lastId is null) order by id")
    List<Article> nextPage(@Nullable Long lastId, Limit limit);

    @Query("select count(a) from Article a where a.publishedAt between :left and :right")
    long publishCountInRange(LocalDate left, LocalDate right);
}
