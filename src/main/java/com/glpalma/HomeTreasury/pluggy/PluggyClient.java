package com.glpalma.HomeTreasury.pluggy;

import com.glpalma.HomeTreasury.config.PluggyProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class PluggyClient {

    private final RestClient restClient;
    private final PluggyProperties properties;

    public PluggyClient(RestClient pluggyRestClient, PluggyProperties properties) {
        this.restClient = pluggyRestClient;
        this.properties = properties;
    }

    private RestClient.RequestHeadersSpec<?> authorizedGet(String uri) {
        return restClient.get()
                .uri(uri)
                .header("X-API-KEY", getApiKey());
    }

    public String getApiKey() {
        PluggyAuthResponse response = restClient.post()
                .uri("/auth")
                .body(Map.of(
                        "clientId", properties.clientId(),
                        "clientSecret", properties.clientSecret()
                ))
                .retrieve()
                .body(PluggyAuthResponse.class);
        if (response == null || response.apiKey() == null) {
            throw new IllegalStateException("Pluggy auth returned empty apiKey");
        }
        return response.apiKey();
    }

    public PluggyAccountsResponse getAccounts() {
        return authorizedGet("/accounts?itemId=" + properties.itemId())
                .retrieve()
                .body(PluggyAccountsResponse.class);
    }

    public PluggyTransactionsResponse getTransactions(String accountId) {
        return authorizedGet("/v2/transactions?accountId=" + accountId)
                .retrieve()
                .body(PluggyTransactionsResponse.class);
    }
}
