package com.example.demo.dto;

import lombok.Data;

@Data
public class OtpVerifyRequest {
    private String phone;
    private String code;
}
