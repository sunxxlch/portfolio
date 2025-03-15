package release.portfolio.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class jwtRefreshService {


    private String REFRESH_SECRET;

    public jwtRefreshService(){
        REFRESH_SECRET = generateSecretKey();
    }

    public String generateSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey secretKey = keyGen.generateKey();
            System.out.println("Secret Key : " + secretKey.toString());
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating secret key", e);
        }
    }

    public String generateRefreshToken(String username ,int time) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + time)) // 7 days
                .signWith(getKey(REFRESH_SECRET), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey(String secret) {
        byte[] bytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(bytes);
    }

    public boolean validateRefreshToken(String token) {
        try {
            Date expiration = RefreshextractClaim(token, Claims::getExpiration);
            System.out.println("Token Expiration Time: " + expiration);
            System.out.println("Current Time: " + new Date());

            return expiration.after(new Date());
        } catch (JwtException e) {
            System.out.println(false);
            return false;
        }
    }

    public String RefreshextractUsername(String token) {
        return RefreshextractClaim(token, Claims::getSubject);
    }

    private <T> T RefreshextractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = RefreshextractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims RefreshextractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey(REFRESH_SECRET))
                .build().parseClaimsJws(token).getBody();
    }


}
