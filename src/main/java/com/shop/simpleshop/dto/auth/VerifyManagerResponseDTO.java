package com.shop.simpleshop.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response DTO for the manager-override endpoint. Returns a fresh access token
 * carrying a short-lived {@code override} claim.
 */
@Getter
@AllArgsConstructor
public class VerifyManagerResponseDTO {

    private String accessToken;
}