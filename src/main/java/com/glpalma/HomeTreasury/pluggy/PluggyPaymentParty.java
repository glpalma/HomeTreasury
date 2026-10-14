package com.glpalma.HomeTreasury.pluggy;

/** One side (payer or receiver) of a Pluggy {@code paymentData}. Only the name is used. */
public record PluggyPaymentParty(String name) {
}
