package me.didi.PWMBackend.tasks;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.didi.PWMBackend.repository.TokenRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupTask {
	private final TokenRepository tokenRepository;

	@Scheduled(fixedDelay = 3600000)
	public void deleteTokens() {
		int deletedTokenAmount = tokenRepository.deleteRevokedAndExpiredTokens();
		log.info("DEBUG: Deleted Tokens (" + deletedTokenAmount + ")");
	}
}
