package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private final String refreshtoken;
    private final String accesstoken;
    private final boolean isNewUser;
}
