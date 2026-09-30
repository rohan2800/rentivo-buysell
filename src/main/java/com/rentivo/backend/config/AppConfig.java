package com.rentivo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /** Injected everywhere time is read so tests can pin it. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
