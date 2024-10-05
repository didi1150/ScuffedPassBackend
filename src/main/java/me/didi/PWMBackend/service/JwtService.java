package me.didi.PWMBackend.service;

import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.model.AuthenticationResponse;
import me.didi.PWMBackend.model.table.Token;
import me.didi.PWMBackend.model.table.User;
import me.didi.PWMBackend.repository.TokenRepository;

@Service
@RequiredArgsConstructor
public class JwtService {
	private final UserService userService;
	private final TokenRepository tokenRepository;
	@Value("${security.jwt.secret-key}")
	private String secretKey;
	@Value("${security.jwt.expiration}")
	private long jwtExpiration;
	@Value("${security.jwt.refresh-token.expiration}")
	private long refreshExpiration;

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String generateToken(User user) {
		return generateToken(new HashMap<>(), user);
	}

	public String generateToken(Map<String, Object> extraClaims, User user) {
		return buildToken(extraClaims, user, jwtExpiration);
	}

	public String generateRefreshToken(User user) {
		return buildToken(new HashMap<>(), user, refreshExpiration);
	}

	private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
		return Jwts.builder().setClaims(extraClaims).setSubject(user.getEmail())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
	}

	private Key getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public int getTokenMaxAge(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();

		Date expiration = claims.getExpiration();
		long maxAgeInMillis = expiration.getTime() - System.currentTimeMillis();
		return (int) (maxAgeInMillis / 1000); // Convert to seconds
	}

	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String userEmail;

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.sendError(401, "No token found in request");
			throw new IOException("No token found in request");
		}
		String refreshToken = authHeader.substring(7);
		userEmail = extractUsername(refreshToken);
		if (userEmail != null) {
			var user = userService.findByEmail(userEmail);
			if (isTokenValid(refreshToken, user)) {
				String accessToken = generateToken(user);
				String newRefreshToken = generateRefreshToken(user);
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
