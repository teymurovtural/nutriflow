package com.nutriflow.security;

import com.nutriflow.exceptions.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j // Added for logging
public class JwtService {

    @Value("${nutriflow.jwt.secret}")
    private String secretKey;

    @Value("${nutriflow.jwt.expiration}")
    private long jwtExpiration;

    @Value("${nutriflow.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        log.debug("Extracting username from token...");
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.error("Error while extracting username: {}", e.getMessage());
            throw new InvalidTokenException("Could not extract username from token: " + e.getMessage());
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        log.info("Generating new Access Token: {}", userDetails.getUsername());
        Map<String, Object> extraClaims = new HashMap<>();

        extraClaims.put("authorities", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        log.info("Generating new Refresh Token: {}", userDetails.getUsername());
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.debug("Checking token validity: {}", userDetails.getUsername());
        try {
            final String username = extractUsername(token);
            boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
            if (!isValid) {
                log.warn("Token is invalid: Username mismatch or expired. User: {}", userDetails.getUsername());
            }
            return isValid;
        } catch (Exception e) {
            log.error("Error occurred while validating token: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        boolean isExpired = expiration.before(new Date());
        if (isExpired) {
            log.warn("Token has expired. Expiration date: {}", expiration);
        }
        return isExpired;
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        log.trace("Parsing JWT Claims...");
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
            throw new InvalidTokenException("JWT token has expired");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT format: {}", e.getMessage());
            throw new InvalidTokenException("JWT token is not supported");
        } catch (MalformedJwtException e) {
            log.error("JWT format is malformed: {}", e.getMessage());
            throw new InvalidTokenException("JWT token is malformed");
        } catch (SignatureException e) {
            log.error("JWT signature mismatch: {}", e.getMessage());
            throw new InvalidTokenException("JWT signature is invalid");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims are empty: {}", e.getMessage());
            throw new InvalidTokenException("JWT claim is empty");
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}