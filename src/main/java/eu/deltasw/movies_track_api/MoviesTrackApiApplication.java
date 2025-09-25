package eu.deltasw.movies_track_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = { org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration.class })
@EnableScheduling
public class MoviesTrackApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviesTrackApiApplication.class, args);
	}

}