package com.glpalma.HomeTreasury.pluggy;

import com.glpalma.HomeTreasury.home.PluggyItem;

import java.time.Instant;

public record PluggyItemResponse(
        String itemId,
        String connectorName,
        Instant linkedAt,
        String status,
        String executionStatus,
        /** When Pluggy last finished fetching data for this item; null if never. */
        Instant lastUpdatedAt
) {

    /** @param details Live status from Pluggy; null when it couldn't be fetched. */
    public static PluggyItemResponse from(PluggyItem item, PluggyItemDetails details) {
        return new PluggyItemResponse(
                item.getItemId(),
                item.getConnectorName(),
                item.getLinkedAt(),
                details == null ? null : details.status(),
                details == null ? null : details.executionStatus(),
                details == null ? null : details.lastUpdatedAt()
        );
    }
}
