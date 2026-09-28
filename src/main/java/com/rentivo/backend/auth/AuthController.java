package com.rentivo.backend.auth;

import com.rentivo.backend.auth.AuthDtos.AuthResponse;
import com.rentivo.backend.auth.AuthDtos.SendOtpRequest;
import com.rentivo.backend.auth.AuthDtos.SendOtpResponse;
import com.rentivo.backend.auth.AuthDtos.VerifyOtpRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/send-otp")
    public SendOtpResponse sendOtp(@Valid @RequestBody SendOtpRequest request) {
        return service.sendOtp(request.phone());
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return service.verify(request);
    }
}
