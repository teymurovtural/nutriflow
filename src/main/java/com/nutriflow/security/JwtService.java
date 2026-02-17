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
@Slf4j // Loglama üçün əlavə edildi
public class JwtService {

    @Value("${nutriflow.jwt.secret}")
    private String secretKey;

    @Value("${nutriflow.jwt.expiration}")
    private long jwtExpiration;

    @Value("${nutriflow.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        log.debug("Token-dən istifadəçi adı çıxarılır...");
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.error("Username çıxarılarkən xəta: {}", e.getMessage());
            throw new InvalidTokenException("Token-dən username çıxarıla bilmədi: " + e.getMessage());
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        log.info("Yeni Access Token yaradılır: {}", userDetails.getUsername());
        Map<String, Object> extraClaims = new HashMap<>();

        extraClaims.put("authorities", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        log.info("Yeni Refresh Token yaradılır: {}", userDetails.getUsername());
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
        log.debug("Token etibarlılığı yoxlanılır: {}", userDetails.getUsername());
        try {
            final String username = extractUsername(token);
            boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
            if (!isValid) {
                log.warn("Token etibarsızdır: Username uyğunsuzluğu və ya vaxtı bitib. User: {}", userDetails.getUsername());
            }
            return isValid;
        } catch (Exception e) {
            log.error("Token doğrulanarkən xəta baş verdi: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        boolean isExpired = expiration.before(new Date());
        if (isExpired) {
            log.warn("Token-in vaxtı bitib. Expiration date: {}", expiration);
        }
        return isExpired;
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        log.trace("JWT Claims parse edilir...");
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token-in vaxtı bitib: {}", e.getMessage());
            throw new InvalidTokenException("JWT token-in müddəti bitib");
        } catch (UnsupportedJwtException e) {
            log.error("Desteklenmeyen JWT formatı: {}", e.getMessage());
            throw new InvalidTokenException("JWT token dəstəklənmir");
        } catch (MalformedJwtException e) {
            log.error("JWT formatı zədələnib (Malformed): {}", e.getMessage());
            throw new InvalidTokenException("JWT token eybəcdir");
        } catch (SignatureException e) {
            log.error("JWT imza uyğunsuzluğu: {}", e.getMessage());
            throw new InvalidTokenException("JWT imzası yanlışdır");
        } catch (IllegalArgumentException e) {
            log.error("JWT claim-ləri boşdur: {}", e.getMessage());
            throw new InvalidTokenException("JWT claim-i boşdur");
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}