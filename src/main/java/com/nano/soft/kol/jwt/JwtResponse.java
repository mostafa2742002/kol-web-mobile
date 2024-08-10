package com.nano.soft.kol.jwt;

import com.nano.soft.kol.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String refreshToken;
    private Object userOrBloger;
    private String intent;
}
