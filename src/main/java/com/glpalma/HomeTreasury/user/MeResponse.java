package com.glpalma.HomeTreasury.user;

public record MeResponse(String email, String role, HomeSummary home) {

    public record HomeSummary(Long id, String name) {
    }
}