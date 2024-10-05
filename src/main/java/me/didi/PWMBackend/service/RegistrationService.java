package me.didi.PWMBackend.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.config.AppConfig;
import me.didi.PWMBackend.model.RegisterRequest;
import me.didi.PWMBackend.model.table.ConfirmationToken;
import me.didi.PWMBackend.model.table.Role;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.tasks.AccountCleanupTask;

@Service
@RequiredArgsConstructor
public class RegistrationService {

	@Value("${spring.custom.domain}")
	private String domainName;

	private final EmailValidator emailValidator;
	private final UserService userService;
	private final RoleService roleService;
	private final PasswordEncoder passwordEncoder;
	private final ConfirmationTokenService confirmationTokenService;
	private final EmailService emailService;
	private final ResourceLoader resourceLoader;
	private final AccountCleanupTask accountCleanupTask;

	public void register(RegisterRequest request) {
		if (!emailValidator.test(request.getEmail())) {
			throw new IllegalStateException("Email is not valid");
		}

		try {
			userService.findByEmail(request.getEmail());
			return;
		} catch (NoSuchElementException e) {

			Role defaultRole = roleService.findById(Long.parseLong("1"));
			if (defaultRole == null) {
				roleService.saveRole("user");
				defaultRole = new Role(Long.parseLong("1"), "user");
			}
			Set<Role> roles = new HashSet<Role>();
			roles.add(defaultRole);
			String salt = request.getSalt();
			var user = User.builder().email(request.getEmail())
					.password(passwordEncoder.encode(salt + request.getPassword())).roles(roles).salt(salt).build();
			userService.saveUser(user);
			String token = UUID.randomUUID().toString();

			ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(),
					LocalDateTime.now().plus(AppConfig.ACC_DEL_DELAY, ChronoUnit.MILLIS), user);

			confirmationTokenService.saveConfirmationToken(confirmationToken);

			String link = domainName + "/register/confirm?token=" + token;
			emailService.send(request.getEmail(), buildEmail(request.getEmail(), link));
			accountCleanupTask.cleanupRegistrationRubbish();
		}
	}

	@Transactional
	public String confirmToken(String token) {
		ConfirmationToken confirmationToken = confirmationTokenService.getToken(token)
				.orElseThrow(() -> new IllegalStateException("token not found"));

		if (confirmationToken.getConfirmedAt() != null) {
			throw new IllegalStateException("email already confirmed");
		}

		LocalDateTime expiredAt = confirmationToken.getExpiresAt();

		if (expiredAt.isBefore(LocalDateTime.now())) {
			throw new IllegalStateException("token expired");
		}

		confirmationTokenService.setConfirmedAt(token);
		userService.enableUserEmail(confirmationToken.getUser().getEmail());
		return "confirmed";
	}

	private String buildEmail(String name, String link) {
		Resource resource = resourceLoader.getResource("classpath:templates/confirmemail-template.html");
		try {
			// Load the template file as a string
			String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

			// Replace placeholders with actual values
			content = content.replace("{{name}}", name);
			content = content.replace("{{link}}", link);

			return content;
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load email template", e);
		}
	}

}
