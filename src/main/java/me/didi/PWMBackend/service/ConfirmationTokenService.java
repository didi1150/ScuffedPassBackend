package me.didi.PWMBackend.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import me.didi.PWMBackend.model.table.ConfirmationToken;
import me.didi.PWMBackend.repository.ConfirmationTokenRepository;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {
	private final ConfirmationTokenRepository confirmationTokenRepository;

	public void saveConfirmationToken(ConfirmationToken token) {
		confirmationTokenRepository.save(token);
	}

	public Optional<ConfirmationToken> getToken(String token) {
		return confirmationTokenRepository.findByToken(token);
	}

	public int setConfirmedAt(String token) {
		return confirmationTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
	}

	public int deleteExpiredToken(Long userId) {
		return confirmationTokenRepository.deleteExpiredToken(LocalDateTime.now(), userId);
	}
}
