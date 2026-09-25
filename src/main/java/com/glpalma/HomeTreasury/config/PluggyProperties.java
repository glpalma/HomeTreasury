package com.glpalma.HomeTreasury.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pluggy")
public record PluggyProperties(String clientId, String clientSecret) {
}
