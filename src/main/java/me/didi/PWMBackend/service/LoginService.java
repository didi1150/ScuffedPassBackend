package me.didi.PWMBackend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.authentication.LoginRequest;
import me.didi.PWMBackend.model.authentication.LoginResponse;
import me.didi.PWMBackend.model.table.User;

@Service
@RequiredArgsConstructor
public class LoginService {

	private final UserService userService;
	private final JwtSaveService jwtSaveService;
	private final JwtUtilService jwtUtilService;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final CookingService cook;
	private final EmailValidator emailValidator;

	public LoginResponse login(LoginRequest request, HttpServletResponse response) throws Exception {
		if (!emailValidator.test(request.getEmail())) {
			throw new IllegalStateException("Email is not valid");
		}

		String salt = cook.retrieveSalt(request.getEmail());
		User user = userService.findByEmail(request.getEmail());
		String encodedPassword = user.getPassword();
		if (!passwordEncoder.matches(salt + request.getPassword(), encodedPassword)) {
			throw new Exception("Passwords don't match");
		}
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), salt + request.getPassword()));
		var jwtToken = jwtUtilService.generateToken(user);
		var refreshToken = jwtUtilService.generateRefreshToken(user);
		jwtSaveService.revokeAllUserTokens(user);
		jwtSaveService.saveUserToken(user, jwtToken);

		return LoginResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).salt(salt)
				.firstLogin(user.isFirstLogin()).build();
	}
}
