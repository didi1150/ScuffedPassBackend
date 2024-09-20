package me.didi.PWMBackend.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CookingService {

	private static final int SALT_LENGTH = 16;
	private final UserService userService;

	public String generateSalt(String email, String password) {
		SecureRandom random = new SecureRandom();
		byte[] randomSalt = new byte[SALT_LENGTH];
		random.nextBytes(randomSalt);

		String combined = email + Base64.getEncoder().encodeToString(randomSalt) + password;

		return hashString(combined);
	}

	private String hashString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashedBytes = md.digest(input.getBytes());
			return Base64.getEncoder().encodeToString(hashedBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Error generating hash", e);
		}
	}

	public String retrieveSalt(String email) {
		return userService.findByEmail(email).getSalt();
	}

}
