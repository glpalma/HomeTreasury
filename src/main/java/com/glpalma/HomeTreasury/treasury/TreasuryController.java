package com.glpalma.HomeTreasury.treasury;

import com.glpalma.HomeTreasury.pluggy.PluggyAccountsResponse;
import com.glpalma.HomeTreasury.pluggy.PluggyClient;
import com.glpalma.HomeTreasury.pluggy.PluggyTransactionsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TreasuryController {
    private final PluggyClient pluggyClient;

    public TreasuryController(PluggyClient pluggyClient) {
        this.pluggyClient = pluggyClient;
    }

    @GetMapping("/ping-pluggy")
    public Map<String, String> pingPluggy() {
        String apiKey = pluggyClient.getApiKey();
        return Map.of("status", "ok", "apiKeyPrefix", apiKey.substring(0, 8) + "...");
    }

    @GetMapping("/saldo")
    public PluggyAccountsResponse saldo() {
        return pluggyClient.getAccounts();
    }

    @GetMapping("/extrato")
    public PluggyTransactionsResponse extrato(
            @RequestParam(required = false) String accountId) {
        if (accountId == null || accountId.isBlank()) {
            var accounts = pluggyClient.getAccounts().results();
            if (accounts == null || accounts.isEmpty()) {
                throw new IllegalStateException("Nenhuma conta encontrada no item");
            }
            accountId = accounts.getFirst().id();
        }
        return pluggyClient.getTransactions(accountId);
    }
}
