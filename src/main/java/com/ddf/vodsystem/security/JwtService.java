package com.ddf.vodsystem.security;

import java.util.Date;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;

public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
    private static final String USER_ID_CLAIM = "userId";
    private static final String ISSUER = "vodsystem";

    public String generateToken(Long userId) {
        return JWT.create()
                .withClaim(USER_ID_CLAIM, userId)
                .withIssuer(ISSUER)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiration))
                .sign(algorithm);
    }

    public boolean validateTokenForId(String token, Long userId) {
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim(USER_ID_CLAIM, userId)
                    .withIssuer(ISSUER)
                    .build();

            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim(USER_ID_CLAIM).asLong().equals(userId) && !jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
