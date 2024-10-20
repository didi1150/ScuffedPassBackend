package me.didi.PWMBackend.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.CryptoRequest;
import me.didi.PWMBackend.model.CryptoResponse;
import me.didi.PWMBackend.model.HashRequest;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.service.CookingService;
import me.didi.PWMBackend.service.PasswordService;
import me.didi.PWMBackend.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "api/auth/account/user")
public class UserController {

	private final CookingService cookingService;
	private final PasswordService passwordService;
	private final UserService userService;

	@GetMapping
	public ResponseEntity<String> getUserName(HttpServletRequest request, Authentication authentication) {

		return authentication == null ? new ResponseEntity<String>(null, null, 403)
				: ResponseEntity.ok(authentication.getName());
	}

	@GetMapping("/salt")
	public ResponseEntity<String> getSalt(Authentication authentication) {
		return authentication.getName() == null ? new ResponseEntity<String>(null, null, 403)
				: ResponseEntity.ok(cookingService.retrieveSalt(authentication.getName()));
	}

	@PostMapping("/encryptionKey")
	public ResponseEntity<CryptoResponse> checkMPassword(@RequestBody HashRequest hashRequest,
			Authentication authentication) {
		if (authentication != null && authentication.getName() != null) {
			if (passwordService.isMasterPasswordCorrect(authentication.getName(), hashRequest.getHash())) {
				User user = userService.findByEmail(authentication.getName());
				return ResponseEntity.ok(CryptoResponse.builder().encryptionKey(user.getEncryptionKey())
						.privateKeyMaster(user.getPrivateKeyMaster()).iv(user.getIv()).build());
			} else {
				return new ResponseEntity<CryptoResponse>(null, null, 401);
			}
		} else
			return new ResponseEntity<CryptoResponse>(null, null, 402);

	}

	@PostMapping("/crypto")
	public void addKeysToAccount(@RequestBody CryptoRequest cryptoRequest, Authentication authentication) {
		userService.addCryptoDetails(cryptoRequest, authentication.getName());
	}
}
