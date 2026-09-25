package com.glpalma.HomeTreasury.pluggy;

public record PluggyItemDetails(String id, Connector connector, String status) {

    public record Connector(String name) {
    }
}
