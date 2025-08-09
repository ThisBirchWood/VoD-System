package com.ddf.vodsystem.security;

import java.util.Date;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final long jwtExpiration;
    private final Algorithm algorithm; // Algorithm is now initialized after secret key is available

    private static final String USER_ID_CLAIM = "userId";
    private static final String ISSUER = "vodsystem";

    public JwtService(@Value("${jwt.secret.key}") String jwtSecretKey,
                      @Value("${jwt.expiration}") long jwtExpiration) {
        this.jwtExpiration = jwtExpiration;
        this.algorithm = Algorithm.HMAC256(jwtSecretKey);
    }

    public String generateToken(Long userId) {
        return JWT.create()
                .withClaim(USER_ID_CLAIM, userId)
                .withIssuer(ISSUER)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
                .sign(algorithm);
    }

    public Long validateTokenAndGetUserId(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaimPresence(USER_ID_CLAIM)
                    .withIssuer(ISSUER)
                    .build();

            DecodedJWT jwt = verifier.verify(token);

            if (jwt.getExpiresAt() == null || jwt.getExpiresAt().before(new Date())) {
                return null;
            }

            return jwt.getClaim(USER_ID_CLAIM).asLong();
        } catch (JwtException | IllegalArgumentException | TokenExpiredException ignored) {
            return null;
        }
    }
}
