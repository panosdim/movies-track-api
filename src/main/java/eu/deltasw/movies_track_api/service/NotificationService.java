package eu.deltasw.movies_track_api.service;

import eu.deltasw.movies_track_api.model.dto.MovieNotifyRequest;

public interface NotificationService {
    void sendNotification(MovieNotifyRequest request);
}
