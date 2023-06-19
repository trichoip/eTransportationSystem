package com.etransportation.security.jwt;

import com.etransportation.config.ApplicationProperties;
import com.etransportation.security.service.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "role";
    private final Key key;
    private final Key refreshKey;
    private final long accessExpireTime;
    private final long refreshExpireTime;
    private final JwtParser jwtParser;
    private final JwtParser refreshJwtParser;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public JwtTokenProvider(ApplicationProperties properties) {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getAccessSecretKey()));
        refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getRefreshSecretKey()));
        accessExpireTime = properties.getAccessExpireTime();
        refreshExpireTime = properties.getRefreshExpireTime();
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
        refreshJwtParser = Jwts.parserBuilder().setSigningKey(refreshKey).build();
    }

    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
        long now = (new Date()).getTime();
        Date validity = new Date(now + 1000 * 60 * this.accessExpireTime);
        return Jwts
            .builder()
            .claim(AUTHORITIES_KEY, authorities)
            .setSubject(authentication.getName())
            .setIssuedAt(new Date(now))
            .setExpiration(validity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public String generateAccessToken(String refreshToken) {
        Claims refreshClaims = refreshJwtParser.parseClaimsJws(refreshToken).getBody();
        String username = refreshClaims.getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return createToken(authentication);
    }

    public String generateRefreshToken(String username) {
        Date expiredTime = new Date((new Date()).getTime() + 1000 * 60 * refreshExpireTime);
        return Jwts.builder().setSubject(username).setExpiration(expiredTime).signWith(refreshKey, SignatureAlgorithm.HS256).compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();
        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public boolean validateToken(String authToken) {
        try {
            jwtParser.parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            refreshJwtParser.parseClaimsJws(refreshToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
