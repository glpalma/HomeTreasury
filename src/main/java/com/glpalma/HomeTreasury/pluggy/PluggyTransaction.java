package com.glpalma.HomeTreasury.pluggy;

import java.math.BigDecimal;

public record PluggyTransaction(
        String id,
        String description,
        BigDecimal amount,
        String date,
        String type,
        String category
) {
}