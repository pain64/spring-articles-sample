package com.example.demo;

import com.example.demo.dao.ArticlesDao;
import com.example.demo.entity.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {ArticleApiTest.TestClockConfiguration.class}) @AutoConfigureMockMvc
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class ArticleApiTest {

    @TestConfiguration static class TestClockConfiguration {
        @Primary @Bean public Clock testClock() {
            return Clock.fixed(
                Instant.parse("2024-08-22T14:18:10.437696646Z"), ZoneId.of("UTC")
            );
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ArticlesDao articlesDao;

    @BeforeEach void init() {
        articlesDao.save(new Article(
            0, "not in range ", "Mr. Big", "no", LocalDate.of(2000, 8, 22)
        ));

        for (var i = 0; i < 5; i++)
            articlesDao.save(new Article(
                0, "article " + (i + 2), "Mr. Big", "no", LocalDate.of(2024, 8, 22)
            ));
    }

    @WithMockUser(roles = "ADMIN")
    @Test void newArticleBadRole() throws Exception {
        mvc.perform(
            post("/api/add").content("""
                    {
                      "title": "string",
                      "author": "string",
                      "content": "string",
                      "publishedAt": "2024-08-22"
                    }"""
                )
                .contentType("application/json")
        ).andExpect(
            status().is(403)
        );
    }

    @WithMockUser(roles = "PUBLISHER")
    @Test void statBadRole() throws Exception {
        mvc.perform(get("/api/stat")).andExpect(
            status().is(403)
        );
    }

    @WithMockUser(roles = "PUBLISHER")
    @Test void listFirstPage() throws Exception {
        mvc.perform(
            get("/api/list").param("pageSize", "2")
        ).andExpect(
            content().json("""
                [
                    {
                      "id": 1,
                      "title": "not in range ",
                      "author": "Mr. Big",
                      "content": "no",
                      "publishedAt": "2000-08-22"
                    },
                    {
                      "id": 2,
                      "title": "article 2",
                      "author": "Mr. Big",
                      "content": "no",
                      "publishedAt": "2024-08-22"
                    }
                  ]
                """)
        );
    }

    @WithMockUser(roles = "PUBLISHER")
    @Test void listSecondPage() throws Exception {
        mvc.perform(
            get("/api/list")
                .param("lastId", "2")
                .param("pageSize", "2")
        ).andExpect(
            content().json("""
                [
                    {
                      "id": 3,
                      "title": "article 3",
                      "author": "Mr. Big",
                      "content": "no",
                      "publishedAt": "2024-08-22"
                    },
                    {
                      "id": 4,
                      "title": "article 4",
                      "author": "Mr. Big",
                      "content": "no",
                      "publishedAt": "2024-08-22"
                    }
                  ]
                """)
        );
    }

    @WithMockUser(roles = "PUBLISHER")
    @Test void addArticle() throws Exception {
        mvc.perform(
            post("/api/add")
                .content("""
                    {
                         "title": "new article",
                         "author": "some",
                         "content": "NOP",
                         "publishedAt": "2024-08-22"
                    }"""
                )
                .contentType("application/json")
        ).andExpect(
            content().json("7")
        );
    }

    @WithMockUser(roles = "ADMIN")
    @Test void stat() throws Exception {
        mvc.perform(
            get("/api/stat")
        ).andExpect(
            content().json("5")
        );
    }
}
