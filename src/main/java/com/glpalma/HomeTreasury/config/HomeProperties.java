package com.glpalma.HomeTreasury.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "home")
public record HomeProperties(String name, BigDecimal idealBalance) {
}
