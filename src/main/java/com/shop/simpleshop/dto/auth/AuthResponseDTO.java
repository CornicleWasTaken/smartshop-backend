package com.shop.simpleshop.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response DTO for successful login/registration.
 * Field names must match the frontend {@code AuthResponse} type exactly.
 */
@Getter
@AllArgsConstructor
public class AuthResponseDTO {

    private String accessToken;
    private String tokenType;
    private AuthUserDTO user;
}
