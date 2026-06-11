package net.frey.orders.config.jwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final JwtParser parser;

    public JwtTokenProvider() {
        var mySecret = "thisisalongstringthatwillbeover32characterstomakehmachappy";
        var secretKey = Keys.hmacShaKeyFor(mySecret.getBytes(StandardCharsets.UTF_8));
        parser = Jwts.parser().verifyWith(secretKey).build();
    }

    public boolean validateToken(String token) {
        try {
            parser.parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getTenantIdFromJwt(String token) {
        var claims = parser.parseSignedClaims(token).getPayload();

        // Assuming you embedded "tenantId" as a custom claim when generating the JWT
        return claims.get("tenantId", String.class);
    }
}
