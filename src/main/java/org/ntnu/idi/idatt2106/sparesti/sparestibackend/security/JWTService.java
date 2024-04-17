package org.ntnu.idi.idatt2106.sparesti.sparestibackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    private final SecretKey key;

    public JWTService() {
        byte[] keyBytes =
                Base64.getDecoder()
                        .decode(System.getenv("SECRET_KEY").getBytes(StandardCharsets.UTF_8));
        this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String generateToken(UserDetails userDetails, long expirationTimeMinutes) {
        return generateToken(new HashMap<>(), userDetails, expirationTimeMinutes);
    }

    public String generateToken(
            Map<String, Object> extraClaims, UserDetails userDetails, long expirationTimeMinutes) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + Duration.ofMinutes(expirationTimeMinutes).toMillis()))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Applies input function on the claims. The returned type depends on the type
     * T of the input function
     * @param token Token
     * @param claimsTFunction The function applied to claims
     * @return Type of input function
     * @param <T> Type of input function
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    /**
     * Gets all claims
     * @param token Token
     * @return Claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Checks if username in token checks out with the current user and
     * that the token is not expired
     * @param token Token
     * @param userDetails Details of the user trying to retrieve session
     * @return True, if username matches and token has not expired
     */
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
}
