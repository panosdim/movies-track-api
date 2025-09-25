package eu.deltasw.movies_track_api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String firstName;
    private String lastName;
}