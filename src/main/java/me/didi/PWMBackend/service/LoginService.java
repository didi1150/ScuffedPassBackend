package me.didi.PWMBackend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.AuthenticationRequest;
import me.didi.PWMBackend.model.AuthenticationResponse;

@Service
@RequiredArgsConstructor
public class LoginService {

	private final UserService userService;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final CookingService cook;
	private final EmailValidator emailValidator;

	public AuthenticationResponse login(AuthenticationRequest request, HttpServletResponse response) {
		if (!emailValidator.test(request.getEmail())) {
			throw new IllegalStateException("Email is not valid");
		}

		String salt = cook.retrieveSalt(request.getEmail());
		var user = userService.findByEmail(request.getEmail());
		String encodedPassword = user.getPassword();
		if (!passwordEncoder.matches(salt + request.getPassword(), encodedPassword)) {
			return null;
		}
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), salt + request.getPassword()));
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		jwtService.revokeAllUserTokens(user);
		jwtService.saveUserToken(user, jwtToken);

		return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).salt(salt).build();
	}
}
