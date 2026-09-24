package com.glpalma.HomeTreasury.home;

import java.time.Instant;

public record InviteResponse(String code, Instant expiresAt) {
}