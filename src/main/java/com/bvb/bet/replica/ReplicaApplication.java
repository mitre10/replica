package com.bvb.bet.replica;

import com.bvb.bet.replica.config.PropertiesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@PropertySource("application.yml")
@EnableConfigurationProperties(PropertiesConfig.class)
@EnableScheduling
public class ReplicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReplicaApplication.class, args);
    }

}
