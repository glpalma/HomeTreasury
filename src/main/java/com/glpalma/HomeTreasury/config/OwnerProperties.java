package com.glpalma.HomeTreasury.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "owner")
public record OwnerProperties(String email, String password) {
}