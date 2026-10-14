package com.glpalma.HomeTreasury.pluggy;

/**
 * Who sent/received a Pix, TED or boleto payment. Pluggy only fills this in for some
 * connectors, so every field here may be null.
 */
public record PluggyPaymentData(PluggyPaymentParty payer, PluggyPaymentParty receiver) {
}
