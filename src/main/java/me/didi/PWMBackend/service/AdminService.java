package me.didi.PWMBackend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.ReducedUserInformation;
import me.didi.PWMBackend.repository.TokenRepository;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final UserService userService;
	private final ConfirmationTokenService confirmationTokenService;
	private final TokenRepository tokenRepository;
	private final PasswordService passwordService;

	public List<ReducedUserInformation> getUserInformation() {
		List<ReducedUserInformation> list = new ArrayList<ReducedUserInformation>();
		userService.getAllUsers().forEach(user -> {
			list.add(new ReducedUserInformation(user.getId(), user.getEmail(), user.isEnabled(), user.isLocked(),
					user.getTimestamp()));
		});
		return list;
	}

	public void deleteUser(Long userId) {
		System.out.println("Delete request: " + userId);
		confirmationTokenService.deleteExpiredToken(userId);
		tokenRepository.deleteAllTokensOfUser(userId);
		passwordService.deleteAllByUserId(userId);
		userService.deleteUser(userId);
	}
}
