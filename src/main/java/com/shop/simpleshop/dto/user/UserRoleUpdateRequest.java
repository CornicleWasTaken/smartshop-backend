package com.shop.simpleshop.dto.user;

import com.shop.simpleshop.security.AppRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for changing a user's role (ADMIN only).
 */
@Getter
@Setter
@NoArgsConstructor
public class UserRoleUpdateRequest {

    @NotNull(message = "Role is required")
    private AppRole role;
}