package eu.deltasw.movies_track_api.controller;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.deltasw.movies_track_api.model.dto.AuthRequest;
import eu.deltasw.movies_track_api.model.dto.AuthResponse;
import eu.deltasw.movies_track_api.model.entity.User;
import eu.deltasw.movies_track_api.repository.UserRepository;
import eu.deltasw.movies_track_api.security.JwtProperties;
import eu.deltasw.movies_track_api.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            String token = jwtUtil.generateToken(request.getEmail());

            String env = System.getenv("SPRING_PROFILES_ACTIVE");
            boolean isProduction = env != null && env.equalsIgnoreCase("production");
            ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(jwtProperties.getExpirationTimeMs() / 1000);
            if (isProduction) {
                cookieBuilder.secure(true).sameSite("Strict");
            } else {
                cookieBuilder.secure(false).sameSite("None");
            }
            ResponseCookie cookie = cookieBuilder.build();

            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(userOpt.get().getFirstName(), userOpt.get().getLastName()));
        } else {
            log.info("Login failed for user: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
