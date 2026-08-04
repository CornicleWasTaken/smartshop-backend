package com.shop.simpleshop.dto.auth;

import lombok.Builder;
import lombok.Getter;

/**
 * Public user representation returned to the frontend.
 * Field names must match the frontend {@code AuthUser} type exactly.
 */
@Getter
@Builder
public class AuthUserDTO {

    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
