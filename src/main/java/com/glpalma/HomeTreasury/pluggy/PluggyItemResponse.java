package com.glpalma.HomeTreasury.pluggy;

import com.glpalma.HomeTreasury.home.PluggyItem;

import java.time.Instant;

public record PluggyItemResponse(String itemId, String connectorName, Instant linkedAt) {

    public static PluggyItemResponse from(PluggyItem item) {
        return new PluggyItemResponse(item.getItemId(), item.getConnectorName(), item.getLinkedAt());
    }
}
