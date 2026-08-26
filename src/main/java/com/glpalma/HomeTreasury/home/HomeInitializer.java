package com.glpalma.HomeTreasury.home;

import com.glpalma.HomeTreasury.config.HomeProperties;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class HomeInitializer {

    @Bean
    @Order(1)
    ApplicationRunner seedHome(HomeRepository homes, HomeProperties props) {
        return args -> {
            if (homes.count() == 0) {
                homes.save(new Home(props.name(), props.idealBalance()));
            }
        };
    }
}