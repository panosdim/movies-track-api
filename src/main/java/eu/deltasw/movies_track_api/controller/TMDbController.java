package eu.deltasw.movies_track_api.controller;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.deltasw.movies_track_api.model.dto.ErrorResponse;
import eu.deltasw.movies_track_api.model.dto.SearchMovieRequest;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.builders.discover.DiscoverMovieParamBuilder;
import info.movito.themoviedbapi.tools.sortby.DiscoverMovieSortBy;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class TMDbController {

    private final TmdbApi tmdb;

    public TMDbController(TmdbApi tmdb) {
        this.tmdb = tmdb;
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularMovies() {
        DiscoverMovieParamBuilder discoverMovieParamBuilder = new DiscoverMovieParamBuilder();
        discoverMovieParamBuilder.page(1);
        discoverMovieParamBuilder.sortBy(DiscoverMovieSortBy.POPULARITY_DESC);
        discoverMovieParamBuilder.region("GR");

        try {
            return ResponseEntity.ok(tmdb.getDiscover().getMovie(discoverMovieParamBuilder));
        } catch (TmdbException e) {
            log.warn("Cannot communicate with TMDb API {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Cannot communicate with TMDb API"));
        }
    }

    @PostMapping("/search")
    public ResponseEntity<?> search(@Valid @RequestBody SearchMovieRequest term) {
        try {
            return ResponseEntity.ok(tmdb.getSearch().searchMovie(term.getTerm(), false, null, null, null, null, null));
        } catch (TmdbException e) {
            log.warn("Cannot communicate with TMDb API {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Cannot communicate with TMDb API"));
        }
    }

    @PostMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@Valid @RequestBody SearchMovieRequest term) {
        try {
            var results = tmdb.getSearch().searchMovie(term.getTerm(), false, null, null, null, null, null);
            var response = results.getResults().stream()
                    .map(movie -> Arrays.asList(
                            movie.getTitle(),
                            movie.getReleaseDate(),
                            movie.getPosterPath() != null
                                    ? "https://image.tmdb.org/t/p/w92" + movie.getPosterPath()
                                    : null))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (TmdbException e) {
            log.warn("Cannot communicate with TMDb API {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Cannot communicate with TMDb API"));
        }
    }
}
