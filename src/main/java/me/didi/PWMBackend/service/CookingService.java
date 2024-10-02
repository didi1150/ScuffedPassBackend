package me.didi.PWMBackend.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CookingService {

	private final UserService userService;

	public String retrieveSalt(String email) {
		return userService.findByEmail(email).getSalt();
	}

}
