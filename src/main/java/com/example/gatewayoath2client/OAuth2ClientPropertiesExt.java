package com.example.gatewayoath2client;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;

@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OAuth2ClientPropertiesExt extends OAuth2ClientProperties {

    @DurationUnit(MILLIS)
    private Duration connectionTimeout;
    @DurationUnit(MILLIS)
    private Duration readTimeout;
    @DurationUnit(SECONDS)
    private Duration clockSkew;

    public Optional<Duration> getConnectionTimeout() {
        return Optional.ofNullable(connectionTimeout);
    }

    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Optional<Duration> getReadTimeout() {
        return Optional.ofNullable(readTimeout);
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Optional<Duration> getClockSkew() {
        return Optional.ofNullable(clockSkew);
    }

    public void setClockSkew(Duration clockSkew) {
        this.clockSkew = clockSkew;
    }
}
