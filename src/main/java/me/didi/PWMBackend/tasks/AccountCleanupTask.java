package me.didi.PWMBackend.tasks;

import java.time.Instant;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.didi.PWMBackend.config.AppConfig;
import me.didi.PWMBackend.repository.TokenRepository;
import me.didi.PWMBackend.service.ConfirmationTokenService;
import me.didi.PWMBackend.service.UserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCleanupTask {

	private final UserService userService;
	private final TokenRepository tokenRepository;
	private final ConfirmationTokenService confirmationTokenService;
	private final ThreadPoolTaskScheduler taskScheduler;

	public void cleanupRegistrationRubbish() {
		taskScheduler.schedule(new Runnable() {

			@Override
			public void run() {
				userService.getAllUsers().forEach(user -> {
					int success = 0;
					success += confirmationTokenService.deleteExpiredToken(user.getId());
					tokenRepository.deleteAllTokensOfUser(user.getId());
					if (!user.isEnabled() && success > 0)
						userService.deleteUser(user.getId());
					log.info("DEBUG: Deleted user details with id: " + user.getId() + " due to inactivity");
				});

			}
		}, Instant.now().plusMillis(AppConfig.ACC_DEL_DELAY));
	}

}
