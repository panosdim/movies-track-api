package eu.deltasw.movies_track_api.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.deltasw.movies_track_api.model.dto.MovieNotifyRequest;
import eu.deltasw.movies_track_api.model.entity.Movie;
import eu.deltasw.movies_track_api.model.entity.ProviderInfo;
import eu.deltasw.movies_track_api.repository.MovieRepository;
import eu.deltasw.movies_track_api.service.EmailService;
import eu.deltasw.movies_track_api.service.WatchProvidersMapperService;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.tools.TmdbException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WatchProviderInfo {
    private final MovieRepository repository;
    private final TmdbApi tmdb;
    private final WatchProvidersMapperService watchProvidersMapperService;
    private final EmailService emailService;

    @Value("${watch-providers.update.cron}")
    private String updateCron;

    public WatchProviderInfo(MovieRepository repository, @Value("${tmdb.key}") String tmdbKey,
            WatchProvidersMapperService watchProvidersMapperService, EmailService emailService) {
        this.repository = repository;
        this.tmdb = new TmdbApi(tmdbKey);
        this.watchProvidersMapperService = watchProvidersMapperService;
        this.emailService = emailService;
    }

    @PostConstruct
    public void init() {
        log.info("Scheduling watch provider updates with cron: {}", updateCron);
    }

    @Scheduled(cron = "${watchproviders.update.cron}")
    public void updateWatchProvidersInfo() {
        log.info("Updating watch providers info...");

        List<Movie> moviesToUpdate = new ArrayList<>();
        Map<Movie, Set<ProviderInfo>> moviesToNotify = new HashMap<>();

        List<Movie> unwatchedMovies = repository.findAllUnwatchedWithWatchInfo();
        log.info("Found {} unwatched movies to check for provider updates.", unwatchedMovies.size());

        for (Movie movie : unwatchedMovies) {
            try {
                Set<ProviderInfo> existingProviders = movie.getWatchInfo();
                Set<ProviderInfo> newProviders = watchProvidersMapperService.convertTo(
                        tmdb.getMovies().getWatchProviders(movie.getMovieId())
                                .getResults().get("GR"));

                boolean hasNewProviders = newProviders != null && !newProviders.isEmpty();
                @SuppressWarnings("null")
                boolean providersChanged = hasNewProviders && !newProviders.equals(existingProviders);

                if (providersChanged) {
                    log.info("Watch providers changed for movie: {} ({})", movie.getTitle(), movie.getMovieId());
                    movie.setWatchInfo(newProviders);
                    moviesToUpdate.add(movie);
                    moviesToNotify.put(movie, newProviders);
                }
            } catch (TmdbException e) {
                log.error("Error getting watch providers for movie: {} ({})", movie.getTitle(), movie.getMovieId(), e);
            } catch (Exception e) {
                log.error("An unexpected error occurred while processing movie: {} ({})", movie.getTitle(),
                        movie.getMovieId(), e);
            }
        }

        if (!moviesToUpdate.isEmpty()) {
            repository.saveAll(moviesToUpdate);
            sendWatchProviderUpdateNotification(moviesToNotify);
        }

        List<Movie> watchedMovies = repository.findAllWatchedWithWatchInfo();
        if (watchedMovies != null && !watchedMovies.isEmpty()) {
            log.info("Found {} watched movies with provider info to be removed.", watchedMovies.size());
            watchedMovies.forEach(movie -> movie.setWatchInfo(Collections.emptySet()));
            repository.saveAll(watchedMovies);
            log.info("Removed provider info from {} watched movies.", watchedMovies.size());
        }

        log.info("Finished updating watch providers info.");
    }

    private void sendWatchProviderUpdateNotification(Map<Movie, Set<ProviderInfo>> moviesToNotify) {
        if (moviesToNotify.isEmpty()) {
            return;
        }

        log.info("Sending notifications for {} movie updates.", moviesToNotify.size());

        // Group movies by user
        Map<String, List<Movie>> userMovieMap = new HashMap<>();
        for (Movie movie : moviesToNotify.keySet()) {
            repository.findUnwatchedByMovieId(movie.getMovieId()).forEach(userMovie -> {
                userMovieMap.computeIfAbsent(userMovie.getUserId(), k -> new ArrayList<>()).add(movie);
            });
        }

        // Send one email per user
        userMovieMap.forEach((userId, movies) -> {
            List<MovieNotifyRequest> requests = movies.stream().map(movie -> {
                var notifyRequest = new MovieNotifyRequest();
                notifyRequest.setMovieTitle(movie.getTitle());
                notifyRequest.setMoviePoster(movie.getPoster());
                notifyRequest.setProviders(moviesToNotify.get(movie));
                return notifyRequest;
            }).collect(Collectors.toList());

            log.info("Sending notification to user {} for {} movies.", userId, movies.size());
            emailService.sendSummaryNotification(userId, requests);
        });
    }
}