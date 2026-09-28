package com.rentivo.backend.security;

import com.rentivo.backend.user.Role;

/** The authenticated principal placed in the security context for every request. */
public record AuthUser(Long id, String phone, Role role) {
}
