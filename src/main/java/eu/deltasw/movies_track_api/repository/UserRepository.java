package eu.deltasw.movies_track_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.deltasw.movies_track_api.model.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

