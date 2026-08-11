package com.shop.simpleshop.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for the manager-override endpoint. The password is the current
 * user's own password; the endpoint only succeeds for MANAGER/ADMIN users.
 */
@Getter
@Setter
@NoArgsConstructor
public class VerifyManagerRequest {

    @NotBlank(message = "Password is required")
    private String password;
}