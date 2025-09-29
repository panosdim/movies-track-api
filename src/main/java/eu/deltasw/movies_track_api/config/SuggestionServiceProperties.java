package eu.deltasw.movies_track_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "suggestion.service")
@Data
public class SuggestionServiceProperties {
    private String url;
}