package eu.deltasw.movies_track_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = MoviesTrackApiApplication.class)
class MoviesTrackApiApplicationTests {

	@Test
	void contextLoads() {
	}

}