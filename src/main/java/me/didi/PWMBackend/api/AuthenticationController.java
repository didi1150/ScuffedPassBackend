package me.didi.PWMBackend.api;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.AuthenticationRequest;
import me.didi.PWMBackend.model.AuthenticationResponse;
import me.didi.PWMBackend.model.RegisterRequest;
import me.didi.PWMBackend.service.JwtSaveService;
import me.didi.PWMBackend.service.LoginService;
import me.didi.PWMBackend.service.RegistrationService;
import me.didi.PWMBackend.service.UserService;

@RestController
@RequestMapping("/api/auth/account")
@RequiredArgsConstructor
public class AuthenticationController {

	private final LoginService loginService;
	private final RegistrationService registrationService;
	private final JwtSaveService jwtService;
	private final UserService userService;

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
		return userService.confirmLockToken(token, email);
	}

	@GetMapping("/requestlock")
	public void requestLock(@RequestParam String email) {
		userService.requestLockToken(email);
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request,
			HttpServletResponse response) {
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