package me.didi.PWMBackend.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.table.ConfirmationToken;
import me.didi.PWMBackend.model.table.User;

@RequiredArgsConstructor
@Service
public class LockService {

	@Value("${spring.cors.origin}")
	private String domainName;

	@Value("${account.deletion.delay}")
	private long accountDeletionDelay;

	private final ConfirmationTokenService confirmationTokenService;
	private final EmailValidator emailValidator;
	private final EmailService emailService;
	private final UserService userService;
	private final TaskScheduler taskScheduler;

	@Transactional
	public boolean confirmLockToken(String token, String email) {
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

		User user = userService.findByEmail(email);
		user.setLocked(true);
		userService.saveUser(user);
		return true;
	}

	public void requestLockToken(String email) {
		if (!emailValidator.test(email)) {
			throw new IllegalStateException("Email is not valid");
		}
		User user = userService.findByEmail(email);
		String token = UUID.randomUUID().toString();

		ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(),
				LocalDateTime.now().plus(accountDeletionDelay, ChronoUnit.MILLIS), user);

		confirmationTokenService.saveConfirmationToken(confirmationToken);

		String link = domainName + "/confirmlock?token=" + token + "&email=" + email;
		emailService.send(email, link, "confirmlock-template", "Lock your Account");
		taskScheduler.schedule(new Runnable() {
			@Override
			public void run() {
				confirmationTokenService.deleteExpiredToken(user.getId());
			}
		}, Instant.now().plusMillis(accountDeletionDelay));

	}

}
