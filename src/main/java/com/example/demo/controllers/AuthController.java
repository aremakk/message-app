package com.example.demo.controllers;


import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.OtpRequest;
import com.example.demo.dto.OtpVerifyRequest;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    public void sendOtp(@RequestBody OtpRequest request) {
        authService.sendOtp(request.getPhone());
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@RequestBody OtpVerifyRequest request) {
        return authService.verifyOtp(
                request.getPhone(),
                request.getCode()
        );
    }
}