package com.lohith.Lpay.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {

    private String name;
    private String email;
    private String accessToken;
    private String refreshToken;
}
