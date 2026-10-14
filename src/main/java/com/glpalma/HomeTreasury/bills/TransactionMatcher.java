package com.glpalma.HomeTreasury.bills;

import com.glpalma.HomeTreasury.treasury.BankTransaction;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Decides which bank transactions belong to a bill or income. Pure: no Spring, no Pluggy.
 * A transaction matches when its type equals {@code direction} and the keyword appears in its
 * description or counterpart name, ignoring case and accents.
 */
public final class TransactionMatcher {

    public enum Direction {
        /** Money out of the account (bills). */
        DEBIT,
        /** Money into the account (incomes). */
        CREDIT
    }

    private TransactionMatcher() {
    }

    /** All matches, oldest first. */
    public static List<BankTransaction> matches(String keyword, Direction direction, List<BankTransaction> transactions) {
        String needle = normalize(keyword);
        if (needle.isEmpty()) {
            return List.of();
        }
        return transactions.stream()
                .filter(tx -> direction.name().equals(tx.type()))
                .filter(tx -> normalize(nullToEmpty(tx.description()) + " " + nullToEmpty(tx.counterpartName()))
                        .contains(needle))
                .sorted(Comparator.comparing(BankTransaction::date))
                .toList();
    }

    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }
}
