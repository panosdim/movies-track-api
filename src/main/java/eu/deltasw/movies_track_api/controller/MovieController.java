package eu.deltasw.movies_track_api.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.deltasw.movies_track_api.client.SuggestionServiceClient;
import eu.deltasw.movies_track_api.model.dto.AddMovieRequest;
import eu.deltasw.movies_track_api.model.dto.RateRequest;
import eu.deltasw.movies_track_api.model.dto.WatchlistResponse;
import eu.deltasw.movies_track_api.model.entity.Movie;
import eu.deltasw.movies_track_api.model.entity.ProviderInfo;
import eu.deltasw.movies_track_api.model.entity.User;
import eu.deltasw.movies_track_api.repository.MovieRepository;
import eu.deltasw.movies_track_api.service.WatchProvidersMapperService;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.tools.TmdbException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/movies")
@Slf4j
public class MovieController {

    private final MovieRepository repository;
    private final TmdbApi tmdb;
    private final WatchProvidersMapperService watchProvidersMapperService;
    private final SuggestionServiceClient suggestionServiceClient;

    public MovieController(MovieRepository repository, TmdbApi tmdb,
            WatchProvidersMapperService watchProvidersMapperService,
            SuggestionServiceClient suggestionServiceClient) {
        this.repository = repository;
        this.tmdb = tmdb;
        this.watchProvidersMapperService = watchProvidersMapperService;
        this.suggestionServiceClient = suggestionServiceClient;
    }

    @GetMapping("/watched")
    public ResponseEntity<?> getWatchedMovies(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(repository.findByUserIdAndWatchedIsTrue(user.getEmail()));
    }

    @GetMapping("/watchlist")
    public ResponseEntity<?> getWatchlist(@AuthenticationPrincipal User user) {
        var movies = repository.findByUserIdAndWatchedIsFalseOrWatchedIsNull(user.getEmail());

        List<WatchlistResponse> watchlistResponse = movies.stream().map(movie -> {
            Double userScore = null;
            try {
                // Get user score from TMDb API
                var movieInfo = tmdb.getMovies().getDetails(movie.getMovieId(), "en", null);
                userScore = movieInfo.getVoteAverage();
            } catch (TmdbException e) {
                log.warn("Error fetching score for movie {}: {}", movie.getMovieId(), e.getMessage());
            }
            return new WatchlistResponse(
                    movie.getId(),
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getPoster(),
                    userScore,
                    movie.getWatchInfo());
        }).collect(Collectors.toList());

        return ResponseEntity.ok(watchlistResponse);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<?> getSuggestions(@AuthenticationPrincipal User user,
            @RequestParam(value = "numMovies", defaultValue = "10") int numMovies) {
        log.info("Fetching {} suggestions for user {}", numMovies, user.getEmail());
        return suggestionServiceClient.getSuggestions(numMovies, user.getEmail());
    }

    @PostMapping
    public ResponseEntity<?> addMovie(@AuthenticationPrincipal User user,
            @Valid @RequestBody AddMovieRequest addMovie) {
        Movie movie = Movie.builder()
                .movieId(addMovie.getMovieId())
                .title(addMovie.getTitle())
                .poster(addMovie.getPoster())
                .userId(user.getEmail())
                .build();

        // Fetch watch info
        try {
            Set<ProviderInfo> newProviders = watchProvidersMapperService.convertTo(
                    tmdb.getMovies().getWatchProviders(movie.getMovieId())
                            .getResults().get("GR"));
            movie.setWatchInfo(newProviders);
        } catch (TmdbException e) {
            log.error("Error getting watch providers", e);
        }

        Movie savedMovie = repository.save(movie);

        // Notify suggestion service to retrain
        try {
            suggestionServiceClient.train(user.getEmail());
        } catch (Exception e) {
            log.error("Error notifying suggestion service for retrain after addMovie", e);
        }

        return ResponseEntity.ok(savedMovie);
    }

    @PostMapping("/watched/{id}")
    public ResponseEntity<?> setWatched(@AuthenticationPrincipal User user, @PathVariable("id") Long id) {
        return repository.findById(id)
                .filter(m -> m.getUserId().equals(user.getEmail()))
                .map(movie -> {
                    movie.setWatched(true);
                    return ResponseEntity.ok(repository.save(movie));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/rate/{id}")
    public ResponseEntity<?> setRating(@AuthenticationPrincipal User user, @PathVariable("id") Long id,
            @Valid @RequestBody RateRequest rateRequest) {
        // Notify suggestion service to retrain
        try {
            suggestionServiceClient.train(user.getEmail());
        } catch (Exception e) {
            log.error("Error notifying suggestion service for retrain after setRating", e);
        }

        return repository.findById(id)
                .filter(m -> m.getUserId().equals(user.getEmail()))
                .map(movie -> {
                    movie.setRating(rateRequest.getRating());
                    return ResponseEntity.ok(repository.save(movie));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@AuthenticationPrincipal User user, @PathVariable("id") Long id) {
        // Notify suggestion service to retrain
        try {
            suggestionServiceClient.train(user.getEmail());
        } catch (Exception e) {
            log.error("Error notifying suggestion service for retrain after deleteMovie", e);
        }

        return repository.findById(id)
                .filter(m -> m.getUserId().equals(user.getEmail()))
                .map(movie -> {
                    repository.delete(movie);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}