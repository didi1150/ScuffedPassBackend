package me.didi.PWMBackend.tasks;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.didi.PWMBackend.repository.TokenRepository;
import me.didi.PWMBackend.repository.UserRepository;
import me.didi.PWMBackend.service.ConfirmationTokenService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCleanupTask {

	private final UserRepository userRepository;
	private final TokenRepository tokenRepository;
	private final ConfirmationTokenService confirmationTokenService;
	private final ThreadPoolTaskScheduler taskScheduler;

	@Value("${account.deletion.delay}")
	private long accountDeletionDelay;

	public void exterminateInvalidUsers() {
		taskScheduler.schedule(new Runnable() {

			@Override
			public void run() {
				userRepository.findAll().forEach(user -> {
					if (!user.isEnabled()) {
						int success = 0;
						success += confirmationTokenService.deleteExpiredToken(user.getId());
						tokenRepository.deleteAllTokensOfUser(user.getId());
						if (success > 0)
							userRepository.deleteById(user.getId());
						log.info("DEBUG: Deleted user details with id: " + user.getId() + " due to inactivity");
					}
				});

			}
		}, Instant.now().plusMillis(accountDeletionDelay));
	}

}
