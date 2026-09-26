package rentivo_backend.security;
import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.time.Instant; import java.util.*;
@Service public class JwtService { private final SecretKey key; private final long expirationMinutes; public JwtService(@Value("${rentivo.jwt.secret}") String secret,@Value("${rentivo.jwt.expiration-minutes:1440}") long expirationMinutes){ if(secret.length()<32) throw new IllegalArgumentException("JWT_SECRET must be at least 32 characters"); this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expirationMinutes=expirationMinutes; }
 public String generate(Long userId,String phone,String role){ Instant now=Instant.now(); return Jwts.builder().subject(String.valueOf(userId)).claim("phone",phone).claim("role",role).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirationMinutes*60))).signWith(key).compact(); }
 public Long userId(String token){return Long.valueOf(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject());}
 public String role(String token){return (String)Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role");}
 public boolean valid(String token){try{Jwts.parser().verifyWith(key).build().parseSignedClaims(token);return true;}catch(JwtException|IllegalArgumentException e){return false;}}
}
