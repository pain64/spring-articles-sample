package com.example.demo.api;

import com.example.demo.dao.ArticlesDao;
import com.example.demo.entity.Article;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api") @RestController public class ArticlesApi {
    private static final int STAT_DAYS_RANGE = 7;

    @Autowired ArticlesDao articlesDao;
    @Autowired Clock clock;

    record AddArticleRequest(
        @NotNull @Length(max = 100) String title, @NotNull String author,
        @NotNull String content, @NotNull LocalDate publishedAt
    ) { }

    record ArticleDto(
        long id, String title, String author,
        String content, LocalDate publishedAt
    ) { }

    @GetMapping("/list") List<ArticleDto> list(
        @RequestParam(required = false) @Nullable Long lastId,
        @RequestParam @Valid @Max(50) @Min(1) int pageSize
    ) {

        // overall articles count may be calculated:
        // 1. FAST but not precise via RDBMS statistics
        // 2. slow but precise via count(*)

        return articlesDao.nextPage(lastId, Limit.of(pageSize))
            .stream().map(article ->
                new ArticleDto(
                    article.id,
                    article.title,
                    article.author,
                    article.content,
                    article.publishedAt
                )
            ).toList();
    }

    // May be your custom annotation
    @PreAuthorize("hasRole('PUBLISHER')")
    @PostMapping("/add") long add(@Valid @RequestBody AddArticleRequest request) {
        var newArticle = new Article(
            0L,
            request.title,
            request.content,
            request.author,
            request.publishedAt
        );

        articlesDao.save(newArticle);

        return newArticle.id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stat") long stat() {
        var now = LocalDate.now(clock);
        return articlesDao.publishCountInRange(now.minusDays(STAT_DAYS_RANGE), now);
    }
}
