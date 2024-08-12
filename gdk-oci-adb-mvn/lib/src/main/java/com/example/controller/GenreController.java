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

package com.example.controller;

import com.example.domain.Genre;
import com.example.service.GenreService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Status;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static io.micronaut.http.HttpHeaders.LOCATION;
import static io.micronaut.http.HttpStatus.NO_CONTENT;

@ExecuteOn(TaskExecutors.IO) // <1>
@Controller("/genres") // <2>
class GenreController {

    private final GenreService genreService;

    GenreController(GenreService genreService) { // <3>
        this.genreService = genreService;
    }

    @Get("/{id}") // <4>
    public Optional<Genre> show(Long id) {
        return genreService.findById(id); // <5>
    }

    @Put("/{id}/{name}") // <6>
    public HttpResponse<?> update(long id, String name) {
        genreService.update(id, name);
        return HttpResponse
                .noContent()
                .header(LOCATION, URI.create("/genres/" + id).getPath()); // <7>
    }

    @Get("/list") // <8>
    public List<Genre> list(@Valid Pageable pageable) { // <9>
        return genreService.list(pageable);
    }

    @Post // <10>
    public HttpResponse<Genre> save(@Body("name") @NotBlank String name) {
        Genre genre = genreService.save(name);

        return HttpResponse
                .created(genre)
                .headers(headers -> headers.location(URI.create("/genres/" + genre.getId())));
    }

    @Delete("/{id}") // <11>
    @Status(NO_CONTENT)
    public void delete(Long id) {
        genreService.delete(id);
    }
}
