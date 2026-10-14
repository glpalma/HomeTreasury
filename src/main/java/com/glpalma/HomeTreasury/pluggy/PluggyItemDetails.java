package com.glpalma.HomeTreasury.pluggy;

import java.time.Instant;

/**
 * @param status          One of Pluggy's item states, e.g. {@code UPDATED}, {@code UPDATING},
 *                         {@code LOGIN_ERROR}, {@code OUTDATED}, {@code WAITING_USER_INPUT}.
 * @param executionStatus Outcome of the last sync attempt, e.g. {@code SUCCESS}.
 * @param lastUpdatedAt   When Pluggy last finished fetching data for this item; null if never.
 */
public record PluggyItemDetails(String id, Connector connector, String status, String executionStatus, Instant lastUpdatedAt) {

    public record Connector(String name) {
    }
}
