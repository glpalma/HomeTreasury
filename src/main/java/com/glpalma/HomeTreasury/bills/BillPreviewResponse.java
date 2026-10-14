package com.glpalma.HomeTreasury.bills;

import java.math.BigDecimal;
import java.util.List;

/**
 * @param matches       Newest first.
 * @param averageAmount Mean of the matches' amounts, or null when there are none.
 * @param truncated     True when Pluggy had more transactions than we read, so matches may be missing.
 */
public record BillPreviewResponse(
        String keyword,
        int months,
        boolean truncated,
        List<BillPreviewMatch> matches,
        BigDecimal averageAmount
) {
}
