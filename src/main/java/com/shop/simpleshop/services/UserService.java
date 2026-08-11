package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.auth.AuthUserDTO;
import com.shop.simpleshop.entity.User;
import com.shop.simpleshop.exceptions.InsufficientRoleException;
import com.shop.simpleshop.exceptions.UserNotFoundException;
import com.shop.simpleshop.repository.UserRepository;
import com.shop.simpleshop.security.AppRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin-only user management: listing users and changing roles.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AuthUserDTO> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toAuthUserDTO)
                .toList();
    }

    /**
     * Changes a user's role. Guards against an admin demoting themselves, which
     * would otherwise risk locking the account out of admin-only endpoints.
     *
     * @param userId the target user
     * @param role   the new role
     * @param caller the current caller (used to block self-demotion)
     */
    @Transactional
    public AuthUserDTO updateRole(Long userId, AppRole role, Authentication caller) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (caller != null && caller.getName() != null && caller.getName().equals(user.getUsername())) {
            throw new InsufficientRoleException("You cannot change your own role");
        }

        user.setRole(role.name());
        return toAuthUserDTO(userRepository.save(user));
    }

    private AuthUserDTO toAuthUserDTO(User user) {
        return AuthUserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(AppRole.fromDb(user.getRole()).name())
                .build();
    }
}