package rentivo_backend.dto;
public final class AuthDtos { private AuthDtos(){}
 public record SendOtpRequest(String phone){}
 public record SendOtpResponse(String message,String developmentOtp){}
 public record VerifyOtpRequest(String phone,String code,String name){}
 public record AuthResponse(String token,Long userId,String name,String phone,String role){}
}
