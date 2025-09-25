package eu.deltasw.movies_track_api.model.entity;

import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private Integer movieId;
    private String title;
    private String poster;
    private Boolean watched;
    private Integer rating;

    @ElementCollection
    @CollectionTable(name = "movie_providers", joinColumns = @JoinColumn(name = "movie_id"))
    private Set<ProviderInfo> watchInfo;
}