/*
 * Copyright 2024 Oracle and/or its affiliates
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.example.domain.Genre;
import com.example.repository.GenreRepository;
import io.micronaut.core.type.Argument;
import io.micronaut.context.env.Environment;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.micronaut.http.HttpHeaders.LOCATION;
import static io.micronaut.http.HttpStatus.CREATED;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static io.micronaut.http.HttpStatus.NO_CONTENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MicronautTest
class GenreControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testFindNonExistingGenreReturns404() {
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.GET("/genres/99999"));
        });

        assertNotNull(thrown.getResponse());
        assertEquals(NOT_FOUND, thrown.getStatus());
    }

    @Disabled
    @Test
    void testGenreCrudOperations() {

        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.POST("/genres", Collections.singletonMap("name", "DevOps")));
        assertEquals(CREATED, response.getStatus());

        response = client.toBlocking().exchange(
                HttpRequest.POST("/genres", Collections.singletonMap("name", "Microservices")));
        assertEquals(CREATED, response.getStatus());

        Long id = entityId(response);

        Genre genre = client.toBlocking().retrieve(
                HttpRequest.GET("/genres/" + id), Genre.class);
        assertEquals("Microservices", genre.getName());

        response = client.toBlocking().exchange(
                HttpRequest.PUT("/genres/" + id + "/Micro-services", null));
        assertEquals(NO_CONTENT, response.getStatus());

        genre = client.toBlocking().retrieve(
                HttpRequest.GET("/genres/" + id), Genre.class);
        assertEquals("Micro-services", genre.getName());

        List<Genre> genres = client.toBlocking().retrieve(
                HttpRequest.GET("/genres/list"), Argument.listOf(Genre.class));
        assertEquals(2, genres.size());

        genres = client.toBlocking().retrieve(
                HttpRequest.GET("/genres/list?size=1"), Argument.listOf(Genre.class));
        assertEquals(1, genres.size());
        assertEquals("DevOps", genres.get(0).getName());

        genres = client.toBlocking().retrieve(
                HttpRequest.GET("/genres/list?size=1&sort=name,desc"), Argument.listOf(Genre.class));
        assertEquals(1, genres.size());
        assertEquals("Micro-services", genres.get(0).getName());

        genres = client.toBlocking().retrieve(
                HttpRequest.GET("/genres/list?size=1&page=2"), Argument.listOf(Genre.class));
        assertEquals(0, genres.size());

        response = client.toBlocking().exchange(
                HttpRequest.DELETE("/genres/" + id));
        assertEquals(NO_CONTENT, response.getStatus());
    }

    private Long entityId(HttpResponse<?> response) {
        String value = response.header(LOCATION);
        if (value == null) {
            return null;
        }
        String path = "/genres/";
        int index = value.indexOf(path);
        return index == -1 ? null : Long.valueOf(value.substring(index + path.length()));
    }

    @Inject
    GenreRepository genreRepository;

    @AfterEach
    void cleanup() {
        genreRepository.deleteAll();
    }
}
