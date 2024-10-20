package me.didi.PWMBackend.service;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.authentication.LoginResponse;
import me.didi.PWMBackend.model.table.Token;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.repository.TokenRepository;

@Service
@RequiredArgsConstructor
public class JwtSaveService {

	private final TokenRepository tokenRepository;
	private final JwtUtilService jwtUtilService;
	private final UserService userService;

	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String userEmail;

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.sendError(401, "No token found in request");
			throw new IOException("No token found in request");
		}
		String refreshToken = authHeader.substring(7);
		userEmail = jwtUtilService.extractUsername(refreshToken);
		if (userEmail != null) {
			var user = userService.findByEmail(userEmail);
			if (jwtUtilService.isTokenValid(refreshToken, user)) {
				String accessToken = jwtUtilService.generateToken(user);
				String newRefreshToken = jwtUtilService.generateRefreshToken(user);
				revokeAllUserTokens(user);
				saveUserToken(user, accessToken);
				saveUserToken(user, newRefreshToken);
				String salt = user.getSalt();
				LoginResponse authResponse = LoginResponse.builder().accessToken(accessToken)
						.refreshToken(newRefreshToken).salt(salt).build();
				new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
			}
		}
	}

	protected void saveUserToken(User user, String jwtToken) {
		var token = Token.builder().user(user).token(jwtToken).expired(false).revoked(false).build();
		tokenRepository.save(token);
	}

	protected void revokeAllUserTokens(User user) {
		var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
		if (validUserTokens.isEmpty())
			return;
		validUserTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});
		tokenRepository.saveAll(validUserTokens);
	}
}
