package me.didi.PWMBackend.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.ChangeMasterPasswordRequest;
import me.didi.PWMBackend.model.CryptoRequest;
import me.didi.PWMBackend.model.CryptoResponse;
import me.didi.PWMBackend.model.HashRequest;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.service.PasswordService;
import me.didi.PWMBackend.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "api/auth/account/user")
public class UserController {

	private final PasswordService passwordService;
	private final UserService userService;

	@GetMapping
	public ResponseEntity<String> getUserName(HttpServletRequest request, Authentication authentication) {

		return authentication == null ? new ResponseEntity<String>(null, null, 403)
				: ResponseEntity.ok(authentication.getName());
	}

	@PostMapping("/encryptionKey")
	public ResponseEntity<CryptoResponse> checkMPassword(@RequestBody HashRequest hashRequest,
			Authentication authentication) {
		if (authentication != null && authentication.getName() != null) {
			if (passwordService.isMasterPasswordCorrect(authentication.getName(), hashRequest.getHash())) {
				User user = userService.findByEmail(authentication.getName());
				return ResponseEntity.ok(CryptoResponse.builder().encryptionKey(user.getEncryptionKey())
						.privateKeyMaster(user.getPrivateKeyMaster()).iv(user.getIv()).salt(user.getSalt()).build());
			} else {
				return new ResponseEntity<CryptoResponse>(null, null, 401);
			}
		} else
			return new ResponseEntity<CryptoResponse>(null, null, 402);

	}

	@PostMapping("/crypto")
	public void addKeysToAccount(@RequestBody CryptoRequest cryptoRequest, Authentication authentication, HttpServletRequest request) {
		userService.addCryptoDetails(cryptoRequest, authentication.getName());
	}

	@PatchMapping("/updatepw")
	public void changeMasterPassword(@RequestBody ChangeMasterPasswordRequest changeMasterPasswordRequest,
			Authentication authentication) {
		passwordService.updateMasterPassword(authentication.getName(), changeMasterPasswordRequest);
	}
}
