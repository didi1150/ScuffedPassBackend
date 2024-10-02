package me.didi.PWMBackend.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.AuthenticationRequest;
import me.didi.PWMBackend.model.AuthenticationResponse;
import me.didi.PWMBackend.model.RegisterRequest;
import me.didi.PWMBackend.model.table.Role;
import me.didi.PWMBackend.model.table.Token;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.repository.TokenRepository;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final UserService userService;
	private final TokenRepository tokenRepository;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final RoleService roleService;
	private final PasswordEncoder passwordEncoder;
	private final CookingService cook;

	public AuthenticationResponse register(RegisterRequest request) {
		try {
			userService.findByEmail(request.getEmail());
			return null;
		} catch (NoSuchElementException e) {

			Role defaultRole = roleService.findById(Long.parseLong("1"));
			if (defaultRole == null) {
				roleService.saveRole("user");
				defaultRole = new Role(Long.parseLong("1"), "user");
			}
			Set<Role> roles = new HashSet<Role>();
			roles.add(defaultRole);
			String salt = request.getSalt();
			var user = User.builder().email(request.getEmail())
					.password(passwordEncoder.encode(salt + request.getPassword())).roles(roles).salt(salt).build();
			var savedUser = userService.saveUser(user);
			var jwtToken = jwtService.generateToken(user);
			var refreshToken = jwtService.generateRefreshToken(user);
			saveUserToken(savedUser, jwtToken);
			return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
		}
	}

	public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) {
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
		revokeAllUserTokens(user);
		saveUserToken(user, jwtToken);

		return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).salt(salt).build();
	}

	private void saveUserToken(User user, String jwtToken) {
		var token = Token.builder().user(user).token(jwtToken).expired(false).revoked(false).build();
		tokenRepository.save(token);
	}

	private void revokeAllUserTokens(User user) {
		var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
		if (validUserTokens.isEmpty())
			return;
		validUserTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});
		tokenRepository.saveAll(validUserTokens);
	}

	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String userEmail;

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.sendError(401, "No token found in request");
			throw new IOException("No token found in request");
		}
		String refreshToken = authHeader.substring(7);
		userEmail = jwtService.extractUsername(refreshToken);
		if (userEmail != null) {
			var user = userService.findByEmail(userEmail);
			if (jwtService.isTokenValid(refreshToken, user)) {
				String accessToken = jwtService.generateToken(user);
				String newRefreshToken = jwtService.generateRefreshToken(user);
				revokeAllUserTokens(user);
				saveUserToken(user, accessToken);
				saveUserToken(user, newRefreshToken);
				String salt = user.getSalt();
				AuthenticationResponse authResponse = AuthenticationResponse.builder().accessToken(accessToken)
						.refreshToken(newRefreshToken).salt(salt).build();
				new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
			}
		}
	}
}
