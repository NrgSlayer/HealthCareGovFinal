package com.cognizant.healthcaregov.util;

import com.cognizant.healthcaregov.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {
    @Value("${secretKey}")
    String secretKey;
    public String generateToken(UserDetails userDetails)
    {
        Map<String,Object> claims =new HashMap<>();
        claims.put("role",userDetails.getAuthorities().stream().map(auth->auth.getAuthority()).toList());
        return Jwts.builder().
                setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+2000*60*60))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String extractEmail(String token)
    {
        return extractClaim(token,Claims::getSubject);
    }
    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver)
    {
        Claims claims=extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignKey())
                .parseClaimsJws(token)
                .getBody();

    }

    public boolean validateToken(String token,UserDetails userDetails)
    {
        return(userDetails.getUsername().equals(extractEmail(token)) && isTokenNotExpired(token));
    }
    public Date extractExpiration(String token)
    {
        return extractClaim(token,Claims::getExpiration);
    }
    public boolean isTokenNotExpired(String token)
    {
        return(new Date().before(extractExpiration(token)));
    }
    public Key getSignKey()
    {
        return  Keys.hmacShaKeyFor(secretKey.getBytes());
    }

}
