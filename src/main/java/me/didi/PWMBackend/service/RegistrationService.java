package me.didi.PWMBackend.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.authentication.RegisterRequest;
import me.didi.PWMBackend.model.table.ConfirmationToken;
import me.didi.PWMBackend.model.table.Role;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.tasks.AccountCleanupTask;

@Service
@RequiredArgsConstructor
public class RegistrationService {

	@Value("${spring.cors.origin}")
	private List<String> allowedOrigins;

	@Value("${account.deletion.delay}")
	private long accountDeletionDelay;

	private final EmailValidator emailValidator;
	private final UserService userService;
	private final RoleService roleService;
	private final PasswordEncoder passwordEncoder;
	private final ConfirmationTokenService confirmationTokenService;
	private final EmailService emailService;
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
					LocalDateTime.now().plus(accountDeletionDelay, ChronoUnit.MILLIS), user);

			confirmationTokenService.saveConfirmationToken(confirmationToken);

			String link = allowedOrigins.get(0) + "/register/confirm?token=" + token;
			emailService.send(request.getEmail(), link, "confirmemail-template", "Confirm your email");
			accountCleanupTask.exterminateInvalidUsers();
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
}
