package rentivo_backend.controller;
import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import rentivo_backend.dto.AuthDtos.*; import rentivo_backend.service.AuthService;
@RestController @RequestMapping("/api/auth") public class AuthController { private final AuthService service; public AuthController(AuthService s){service=s;} @PostMapping("/send-otp") public SendOtpResponse send(@RequestBody SendOtpRequest r){return service.sendOtp(r.phone());} @PostMapping("/verify-otp") public AuthResponse verify(@RequestBody VerifyOtpRequest r){return service.verify(r);} }
