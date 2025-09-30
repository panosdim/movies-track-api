package eu.deltasw.movies_track_api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "suggestion-service", url = "${suggestion.service.url:}")
public interface SuggestionServiceClient {

    @GetMapping("/suggestions")
    ResponseEntity<Object> getSuggestions(@RequestParam("max_suggestions") int maxSuggestions,
            @RequestParam("user_id") String userId);

    @GetMapping("/train")
    ResponseEntity<Void> train(@RequestParam("user_id") String userId);

}
