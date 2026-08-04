package com.shop.simpleshop.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response DTO for the refresh-token endpoint. Returns a fresh access token.
 */
@Getter
@AllArgsConstructor
public class RefreshResponseDTO {

    private String accessToken;
}
