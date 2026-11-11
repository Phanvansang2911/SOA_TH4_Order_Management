package com.soa.auth;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
@RestController public class AuthController {
 private final UserRepository users; private final TokenRepository tokens;
 private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
 public AuthController(UserRepository users,TokenRepository tokens){this.users=users;this.tokens=tokens;}
 public record Credentials(String email,String password){}
 @PostMapping("/register") public ResponseEntity<?> register(@RequestBody Credentials c){
  if(c==null || c.email()==null || !c.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || c.password()==null || c.password().length()<8)
   return ResponseEntity.badRequest().body(Map.of("message","Email hợp lệ và password tối thiểu 8 ký tự là bắt buộc"));
  String email=c.email().trim().toLowerCase(Locale.ROOT);
  if(users.findByEmail(email).isPresent())return ResponseEntity.status(409).body(Map.of("message","Email đã tồn tại"));
  UserAccount u=users.save(new UserAccount(email,encoder.encode(c.password())));
  return ResponseEntity.status(201).body(Map.of("id",u.id,"email",u.email));
 }
 @PostMapping("/login") public ResponseEntity<?> login(@RequestBody Credentials c){
  if(c==null || c.email()==null || c.password()==null) return ResponseEntity.status(401).body(Map.of("message","Thông tin đăng nhập không hợp lệ"));
  Optional<UserAccount> u=users.findByEmail(c.email().trim().toLowerCase(Locale.ROOT));
  if(u.isEmpty()||!encoder.matches(c.password(),u.get().passwordHash))return ResponseEntity.status(401).body(Map.of("message","Sai email hoặc mật khẩu"));
  byte[] random=new byte[32];new SecureRandom().nextBytes(random);
  String token=Base64.getUrlEncoder().withoutPadding().encodeToString(random);
  Instant exp=Instant.now().plus(Duration.ofHours(8)); tokens.save(new LoginToken(hash(token),u.get(),exp));
  return ResponseEntity.ok(Map.of("access_token",token,"token_type","Bearer","expires_at",exp.toString()));
 }
 @GetMapping("/auth") public ResponseEntity<?> verify(@RequestHeader(value="Authorization",required=false) String header){
  if(header==null||!header.startsWith("Bearer ")||header.substring(7).isBlank())return ResponseEntity.status(401).body(Map.of("message","Thiếu token"));
  return tokens.findByTokenHash(hash(header.substring(7).trim()))
   .filter(t->t.expiresAt.isAfter(Instant.now()))
   .<ResponseEntity<?>>map(t->ResponseEntity.ok(Map.of("valid",true,"user_id",t.user.id,"email",t.user.email)))
   .orElseGet(()->ResponseEntity.status(401).body(Map.of("message","Token không hợp lệ hoặc đã hết hạn")));
 }
 private static String hash(String value){try{
  byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
  return HexFormat.of().formatHex(bytes);
 }catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
