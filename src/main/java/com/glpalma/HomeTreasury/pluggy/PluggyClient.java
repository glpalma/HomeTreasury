package com.glpalma.HomeTreasury.pluggy;

import com.glpalma.HomeTreasury.config.PluggyProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@Component
public class PluggyClient {

    private static final Duration API_KEY_TTL = Duration.ofMinutes(110);

    private final RestClient restClient;
    private final PluggyProperties properties;

    private volatile String cachedApiKey;
    private volatile Instant cachedApiKeyExpiresAt = Instant.EPOCH;

    public PluggyClient(RestClient pluggyRestClient, PluggyProperties properties) {
        this.restClient = pluggyRestClient;
        this.properties = properties;
    }

    private RestClient.RequestHeadersSpec<?> authorizedGet(String uri) {
        return restClient.get()
                .uri(uri)
                .header("X-API-KEY", getApiKey());
    }

    private RestClient.RequestBodySpec authorizedPost(String uri) {
        return restClient.post()
                .uri(uri)
                .header("X-API-KEY", getApiKey());
    }

    public synchronized String getApiKey() {
        if (cachedApiKey != null && Instant.now().isBefore(cachedApiKeyExpiresAt)) {
            return cachedApiKey;
        }
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
        cachedApiKey = response.apiKey();
        cachedApiKeyExpiresAt = Instant.now().plus(API_KEY_TTL);
        return cachedApiKey;
    }

    public PluggyConnectTokenResponse getConnectToken() {
        return authorizedPost("/connect_token")
                .body(Map.of())
                .retrieve()
                .body(PluggyConnectTokenResponse.class);
    }

    public PluggyItemDetails getItem(String itemId) {
        return authorizedGet("/items/" + itemId)
                .retrieve()
                .body(PluggyItemDetails.class);
    }

    public PluggyAccountsResponse getAccounts(String itemId) {
        return authorizedGet("/accounts?itemId=" + itemId)
                .retrieve()
                .body(PluggyAccountsResponse.class);
    }

    public PluggyTransactionsResponse getTransactions(String accountId, LocalDate from, LocalDate to) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/v2/transactions").queryParam("accountId", accountId);
                    if (from != null) {
                        uriBuilder.queryParam("dateFrom", from.toString());
                    }
                    if (to != null) {
                        uriBuilder.queryParam("dateTo", to.toString());
                    }
                    return uriBuilder.build();
                })
                .header("X-API-KEY", getApiKey())
                .retrieve()
                .body(PluggyTransactionsResponse.class);
    }
}
