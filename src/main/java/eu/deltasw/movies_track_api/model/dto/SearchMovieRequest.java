package eu.deltasw.movies_track_api.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchMovieRequest {
    @NotBlank(message = "term is required")
    private String term;
}
