package com.exercises.hotelbooking.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ApplicationHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // TODO: improve check
        return Health.up().build();
    }

}
