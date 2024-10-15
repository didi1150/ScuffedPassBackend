package me.didi.PWMBackend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

	@Value("${spring.mail.username}")
	private String fromEmail;

	private final JavaMailSender mailSender;

	@Async
	public void send(String to, String link, String template) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
			helper.setText(buildEmail(to, link, template), true);
			helper.setTo(to);
			helper.setSubject("Confirm your email");
			helper.setFrom(fromEmail);
			mailSender.send(mimeMessage);
			log.info("Email sent");
		} catch (MessagingException e) {
			log.error("failed to send email", e);
			throw new IllegalStateException("failed to send email");
		}
	}

	private String buildEmail(String name, String link, String template) {
		try (InputStream inputStream = getClass().getResourceAsStream("/templates/" + template + ".html")) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String content = reader.lines().collect(Collectors.joining(System.lineSeparator()));

			content = content.replace("{{name}}", name);
			content = content.replace("{{link}}", link);

			return content;
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load email template", e);
		}
	}
}
