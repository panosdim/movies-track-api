package eu.deltasw.movies_track_api.model.dto;

import java.util.Set;

import eu.deltasw.movies_track_api.model.entity.ProviderInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WatchlistResponse {
    private Long id;
    private Integer movieId;
    private String title;
    private String poster;
    private Double userScore;
    private Set<ProviderInfo> watchInfo;
}
