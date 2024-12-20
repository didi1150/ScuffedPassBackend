package me.didi.PWMBackend.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import me.didi.PWMBackend.model.table.User;

@Service
@RequiredArgsConstructor
public class JwtUtilService {
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
		Map<String, Object> extraClaims = new HashMap<String, Object>();
		extraClaims.put("roles", user.getRoles());
		return generateToken(extraClaims, user);
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
		try {

			return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();

		} catch (Exception e) {
			System.out.println("Error: " + e.getClass().getName());
			return null;
		}
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
}
