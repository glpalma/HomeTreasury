package com.glpalma.HomeTreasury;

import com.glpalma.HomeTreasury.config.HomeProperties;
import com.glpalma.HomeTreasury.config.JwtProperties;
import com.glpalma.HomeTreasury.config.OwnerProperties;
import com.glpalma.HomeTreasury.config.PluggyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({PluggyProperties.class, HomeProperties.class, OwnerProperties.class, JwtProperties.class})
public class HomeTreasuryApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeTreasuryApplication.class, args);
	}

}
