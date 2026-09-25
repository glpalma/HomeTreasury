package com.glpalma.HomeTreasury.pluggy;

import jakarta.validation.constraints.NotBlank;

public record LinkPluggyItemRequest(@NotBlank String itemId) {
}
