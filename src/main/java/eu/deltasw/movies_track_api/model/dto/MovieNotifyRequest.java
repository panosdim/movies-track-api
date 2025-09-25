package eu.deltasw.movies_track_api.model.dto;

import java.util.List;
import java.util.Set;

import eu.deltasw.movies_track_api.model.entity.ProviderInfo;
import lombok.Data;

@Data
public class MovieNotifyRequest {
    private List<String> userIds;
    private String movieTitle;
    private String moviePoster;
    private Set<ProviderInfo> providers = new java.util.HashSet<>();
}
