package com.glpalma.HomeTreasury.pluggy;

import java.math.BigDecimal;
public record PluggyAccount(
        String id,
        String name,
        String type,
        String subtype,
        BigDecimal balance,
        String currencyCode
) {
}