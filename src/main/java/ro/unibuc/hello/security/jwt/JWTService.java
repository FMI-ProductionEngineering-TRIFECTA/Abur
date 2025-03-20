package ro.unibuc.hello.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JWTService {

    private final String secretKey;

    @Autowired
    public JWTService(@Value("${security.jwt.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String getToken(String id) {
        return Jwts
            .builder()
            .claim("userId", id)
            .signWith(getSecretKey())
            .compact();
    }

    public String extractUserId(String token) {
        return Jwts
            .parser()
            .verifyWith(getSecretKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("userId", String.class);
    }

}
