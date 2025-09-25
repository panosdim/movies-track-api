package eu.deltasw.movies_track_api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import eu.deltasw.movies_track_api.model.dto.MovieNotifyRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService implements NotificationService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${tmdb.image.base-url}")
    private String imageBaseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendNotification(MovieNotifyRequest request) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from);
            if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
                helper.setTo(request.getUserIds().toArray(new String[0]));
            }
            helper.setSubject("Movie Notification");

            // Use imageBaseUrl from application.yml
            String imageUrl = imageBaseUrl + "/w300" + request.getMoviePoster();
            String providers = String.join(" ", request.getProviders().stream()
                    .map(provider -> "<img src='" + imageBaseUrl + "/w45" + provider.getLogoPath() + "' title='"
                            + provider.getProviderName() + "' />")
                    .toList());
            String htmlMsg = "<h2>Watch providers updated for movie: " + request.getMovieTitle() + "</h2>" +
                    "<img src='" + imageUrl + "' alt='Movie Image' />" +
                    "<br />" +
                    "<p>New watch providers are available!</p>" +
                    "<div>" + providers + "</div>";
            helper.setText(htmlMsg, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendSummaryNotification(String userId, List<MovieNotifyRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(userId);
            helper.setSubject("Watch Provider Updates for your Watchlist");

            StringBuilder htmlMsg = new StringBuilder(
                    "<h2>Watch providers have been updated for movies on your watchlist!</h2>");

            for (MovieNotifyRequest request : requests) {
                String imageUrl = imageBaseUrl + "/w300" + request.getMoviePoster();
                String providers = String.join(" ", request.getProviders().stream()
                        .map(provider -> "<img src='" + imageBaseUrl + "/w45" + provider.getLogoPath() + "' title='"
                                + provider.getProviderName() + "' style='margin-right: 5px; border-radius: 5px;' />")
                        .toList());

                htmlMsg.append(
                        "<div style='margin-bottom: 20px; padding-bottom: 10px; border-bottom: 1px solid #eee;'>");
                htmlMsg.append("<h3>").append(request.getMovieTitle()).append("</h3>");
                htmlMsg.append("<img src='").append(imageUrl)
                        .append("' alt='Movie Poster' style='max-width: 150px; height: auto;' /><br/>");
                htmlMsg.append("<p>New watch providers are available:</p>");
                htmlMsg.append("<div>").append(providers).append("</div>");
                htmlMsg.append("</div>");
            }

            helper.setText(htmlMsg.toString(), true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send summary email to " + userId, e);
        }
    }
}
