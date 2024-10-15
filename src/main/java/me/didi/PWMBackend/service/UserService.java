package me.didi.PWMBackend.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.table.ConfirmationToken;
import me.didi.PWMBackend.model.table.Role;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

	@Value("${spring.cors.origin}")
	private String domainName;

	@Value("${account.deletion.delay}")
	private long accountDeletionDelay;

	private final UserRepository userRepository;
	private final ConfirmationTokenService confirmationTokenService;
	private final EmailValidator emailValidator;
	private final EmailService emailService;
	private final TaskScheduler taskScheduler;

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public User saveUser(User user) {
		return userRepository.save(user);
	}

	public User findByID(Long id) {
		return userRepository.findById(id).isPresent() ? userRepository.findById(id).get() : null;
	}

	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	public User updateUser(User user) {
		return userRepository.save(user);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = findByEmail(username);
		if (user != null) {
			List<Role> roles = new ArrayList<>(user.getRoles());
			String[] rolesNames = new String[roles.size()];
			for (int i = 0; i < roles.size(); i++) {
				rolesNames[i] = roles.get(i).getName();
			}
			return User.builder().id(user.getId()).email(user.getEmail()).password(user.getPassword())
					.roles(user.getRoles()).enabled(user.isEnabled()).locked(user.isLocked()).build();
		}
		return null;
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email).get();
	}

	public int deleteDisabledUsers() {
		return userRepository.deleteNonVerifiedUsers();
	}

	public void enableUserEmail(String email) {
		User user = findByEmail(email);
		user.setEnabled(true);
		saveUser(user);
	}

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

		User user = findByEmail(email);
		user.setLocked(true);
		saveUser(user);
		return true;
	}

	public void requestLockToken(String email) {
		if (!emailValidator.test(email)) {
			throw new IllegalStateException("Email is not valid");
		}
		User user = findByEmail(email);
		String token = UUID.randomUUID().toString();

		ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(),
				LocalDateTime.now().plus(accountDeletionDelay, ChronoUnit.MILLIS), user);

		confirmationTokenService.saveConfirmationToken(confirmationToken);

		String link = domainName + "/confirmlock?token=" + token + "&email=" + email;
		emailService.send(email, link, "confirmlock-template");
		taskScheduler.schedule(new Runnable() {
			@Override
			public void run() {
				confirmationTokenService.deleteExpiredToken(user.getId());
			}
		}, Instant.now().plusMillis(accountDeletionDelay));

	}
}