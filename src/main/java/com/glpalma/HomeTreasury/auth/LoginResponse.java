package com.glpalma.HomeTreasury.auth;

public record LoginResponse(String token, String email, String role) {
}