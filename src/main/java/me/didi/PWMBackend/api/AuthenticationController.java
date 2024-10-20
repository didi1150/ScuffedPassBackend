package me.didi.PWMBackend.api;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.RecoverPrivateKeyResponse;
import me.didi.PWMBackend.model.RecoveryRequest;
import me.didi.PWMBackend.model.ResetMasterPasswordRequest;
import me.didi.PWMBackend.model.authentication.LoginRequest;
import me.didi.PWMBackend.model.authentication.LoginResponse;
import me.didi.PWMBackend.model.authentication.RegisterRequest;
import me.didi.PWMBackend.service.JwtSaveService;
import me.didi.PWMBackend.service.LockService;
import me.didi.PWMBackend.service.LoginService;
import me.didi.PWMBackend.service.RecoveryService;
import me.didi.PWMBackend.service.RegistrationService;

@RestController
@RequestMapping("/api/auth/account")
@RequiredArgsConstructor
public class AuthenticationController {

	private final LoginService loginService;
	private final RegistrationService registrationService;
	private final JwtSaveService jwtService;
	private final RecoveryService recoveryService;
	private final LockService lockService;

	@PostMapping("/register")
	public void register(@RequestBody RegisterRequest request) {
		registrationService.register(request);
	}

	@GetMapping(path = "confirm")
	public String confirmRegistrationToken(@RequestParam String token) {
		return registrationService.confirmToken(token);
	}

	@GetMapping("/confirmlock")
	public boolean confirmLock(@RequestParam String token, @RequestParam String email) {
		return lockService.confirmLockToken(token, email);
	}

	@GetMapping("/requestlock")
	public void requestLock(@RequestParam String email) {
		lockService.requestLockToken(email);
	}

	@GetMapping("/resetpw")
	public void resetPw(@RequestParam String email) {
		recoveryService.requestPasswordReset(email);
	}

	@PostMapping("/requestrecovery")
	public RecoverPrivateKeyResponse requestRecovery(@RequestBody RecoveryRequest recoveryRequest) {
		return recoveryService.checkIfRecoveryTokenExists(recoveryRequest);
	}

	@PatchMapping("/updatepw")
	public void updatePw(@RequestBody ResetMasterPasswordRequest resetMasterPasswordRequest) {
		recoveryService.resetMasterPassword(resetMasterPasswordRequest);
	}

	@PostMapping("/authenticate")
	public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginRequest request, HttpServletResponse response) {
		return ResponseEntity.ok(loginService.login(request, response));
	}

	@PostMapping("/token-auth")
	public ResponseEntity<?> tokenAuth(HttpServletResponse response) {
		return ResponseEntity.ok("Success");
	}

	@PostMapping("/refresh-token")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		jwtService.refreshToken(request, response);
	}
}