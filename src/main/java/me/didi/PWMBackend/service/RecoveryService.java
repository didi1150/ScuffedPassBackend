package me.didi.PWMBackend.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.RecoverPrivateKeyResponse;
import me.didi.PWMBackend.model.RecoveryRequest;
import me.didi.PWMBackend.model.ResetMasterPasswordRequest;
import me.didi.PWMBackend.model.table.ConfirmationToken;
import me.didi.PWMBackend.model.table.User;

@RequiredArgsConstructor
@Service
public class RecoveryService {

	@Value("${spring.cors.origin}")
	private String domainName;

	@Value("${account.deletion.delay}")
	private long accountDeletionDelay;

	private final ConfirmationTokenService confirmationTokenService;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;

	private final EmailValidator emailValidator;
	private final EmailService emailService;
	private final TaskScheduler taskScheduler;
	private final JwtSaveService jwtSaveService;

	public void resetMasterPassword(ResetMasterPasswordRequest resetMasterPasswordRequest) {
		ConfirmationToken confirmationToken = confirmationTokenService.getToken(resetMasterPasswordRequest.getToken())
				.orElseThrow(() -> new IllegalStateException("token not found"));

		if (confirmationToken.getConfirmedAt() != null) {
			throw new IllegalStateException("token already used");
		}

		LocalDateTime expiredAt = confirmationToken.getExpiresAt();

		if (expiredAt.isBefore(LocalDateTime.now())) {
			throw new IllegalStateException("token expired");
		}

		confirmationTokenService.setConfirmedAt(resetMasterPasswordRequest.getToken());

		User user = confirmationToken.getUser();
		user.setIv(resetMasterPasswordRequest.getIv());
		user.setPrivateKeyMaster(resetMasterPasswordRequest.getPrivateKeyMaster());
		user.setPrivateKeyRecovery(resetMasterPasswordRequest.getPrivateKeyRecovery());
		user.setSalt(resetMasterPasswordRequest.getSalt());
		user.setPassword(passwordEncoder
				.encode(resetMasterPasswordRequest.getSalt() + resetMasterPasswordRequest.getPassword()));
		jwtSaveService.revokeAllUserTokens(user);
		userService.saveUser(user);
	}

	public void requestPasswordReset(String email) {
		if (!emailValidator.test(email)) {
			throw new IllegalStateException("Email is not valid");
		}
		User user = userService.findByEmail(email);
		String token = UUID.randomUUID().toString();

		ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(),
				LocalDateTime.now().plus(accountDeletionDelay, ChronoUnit.MILLIS), user);

		confirmationTokenService.saveConfirmationToken(confirmationToken);

		String link = domainName + "/resetpw?token=" + token + "&email=" + email;
		emailService.send(email, link, "confirmreset-template", "Reset your Password");
		taskScheduler.schedule(new Runnable() {
			@Override
			public void run() {
				confirmationTokenService.deleteExpiredToken(user.getId());
			}
		}, Instant.now().plusMillis(accountDeletionDelay));
	}

	@Transactional
	public RecoverPrivateKeyResponse checkIfRecoveryTokenExists(RecoveryRequest updateResetMasterPassword) {
		confirmationTokenService.getToken(updateResetMasterPassword.getToken())
				.orElseThrow(() -> new IllegalStateException("token not found"));

		User user = userService.findByEmail(updateResetMasterPassword.getEmail());

		// 1. Return private key recovery
		// 2. Decrypt private key, reencrypt with new recovery key + new master password
		// 3. Send privateKeyR and privateKeyM to server
		return RecoverPrivateKeyResponse.builder().privateKeyRecovery(user.getPrivateKeyRecovery()).iv(user.getIv())
				.salt(user.getSalt()).build();
	}

}
