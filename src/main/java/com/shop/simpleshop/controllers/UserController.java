package com.shop.simpleshop.controllers;

import com.shop.simpleshop.dto.auth.AuthUserDTO;
import com.shop.simpleshop.dto.user.UserRoleUpdateRequest;
import com.shop.simpleshop.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Admin endpoints for managing users and roles")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List all users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuthUserDTO> listUsers() {
        return userService.listUsers();
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Change a user's role")
    @PreAuthorize("hasRole('ADMIN')")
    public AuthUserDTO updateRole(@PathVariable Long id,
                                  @Valid @RequestBody UserRoleUpdateRequest request,
                                  Authentication authentication) {
        return userService.updateRole(id, request.getRole(), authentication);
    }
}