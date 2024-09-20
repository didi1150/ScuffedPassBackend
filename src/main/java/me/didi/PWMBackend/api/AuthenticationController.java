package me.didi.PWMBackend.api;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.AuthenticationRequest;
import me.didi.PWMBackend.model.AuthenticationResponse;
import me.didi.PWMBackend.model.RegisterRequest;
import me.didi.PWMBackend.service.AuthenticationService;

@RestController
@RequestMapping("/api/auth/account")
@RequiredArgsConstructor
public class AuthenticationController {

	private final AuthenticationService service;

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
		AuthenticationResponse authenticationResponse = service.register(request);

		return authenticationResponse == null ? new ResponseEntity<AuthenticationResponse>(null, null, 403)
				: ResponseEntity.ok(authenticationResponse);
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request,
			HttpServletResponse response) {
		return ResponseEntity.ok(service.authenticate(request, response));
	}

	@PostMapping("/token-auth")
	public ResponseEntity<?> tokenAuth(HttpServletResponse response) {
		return ResponseEntity.ok("Success");
	}

	@PostMapping("/refresh-token")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		service.refreshToken(request, response);
	}
}