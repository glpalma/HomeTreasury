package com.glpalma.HomeTreasury.pluggy;

import java.util.List;

public record PluggyTransactionsResponse(List<PluggyTransaction> results, String next) {
}