package foodprint.backend.config;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import foodprint.backend.model.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenUtil {

    @Value("${FOODPRINT_JWT_KEY}")
    private String jwtSecret;

    private static final String JWT_ISSUER = "foodprint.io";
    private final Logger loggr = LoggerFactory.getLogger(this.getClass());

    public String generateAccessToken(User user) {
         JwtBuilder token = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer(JWT_ISSUER)
                .setIssuedAt(new Date())
                .setHeaderParam("userId", user.getId())
                .setHeaderParam("userFname", user.getFirstName())
                .setHeaderParam("userLname", user.getLastName())
                .setHeaderParam("userAuthorities", user.getRoles().split(","));

         if (user.getRoles().contains("FP_MANAGER")) {
            token
            .setHeaderParam("restaurantId", user.getRestaurant().getRestaurantId());
         }

         if (user.getVaccinationDob() != null && user.getVaccinationName() != null) {
            token
            .setHeaderParam("vaccinationName", user.getVaccinationName())
            .setHeaderParam("vaccinationDob", user.getVaccinationDob().toString());
         }

         token
         .setExpiration(Date.from(LocalDateTime.now().plus(7, ChronoUnit.DAYS).toInstant(ZoneOffset.UTC)))
         .signWith(SignatureAlgorithm.HS512, jwtSecret);

         return token.compact();
    }

    public String getUserId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId").toString();
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public LocalDateTime getExpirationDate(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(claims.getExpiration().getTime()), ZoneOffset.UTC);
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            loggr.error("Invalid JWT signature - {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            loggr.error("Invalid JWT token - {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            loggr.error("Expired JWT token - {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            loggr.error("Unsupported JWT token - {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            loggr.error("JWT claims string is empty - {}", ex.getMessage());
        }
        return false;
    }

}
