package com.example.gatewayoath2client;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Clock;
import java.time.Duration;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.lang.Math.toIntExact;

@Configuration
class SecurityConfig {

    //See DefaultChannelConfig.DEFAULT_CONNECT_TIMEOUT
    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(30);
    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);
    public static final Duration DEFAULT_CLOCK_SKEW = Duration.ofSeconds(60);

    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .authorizeExchange(authorize -> authorize
                        .anyExchange().permitAll())
                .build();
    }

    @Bean
    @Primary
    OAuth2ClientProperties oAuth2ClientProperties() {
        return new OAuth2ClientPropertiesExt();
    }

    @Bean
    ReactiveOAuth2AuthorizedClientManager gatewayReactiveOAuth2AuthorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
            OAuth2ClientPropertiesExt properties) {

        var webClient = webClient(properties);

        var accessTokenResponseClient = new WebClientReactiveClientCredentialsTokenResponseClient();
        accessTokenResponseClient.setWebClient(webClient);

        var authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(grantType -> grantType
                        .clock(Clock.systemDefaultZone())
                        .clockSkew(properties.getClockSkew().orElse(DEFAULT_CLOCK_SKEW))
                        .accessTokenResponseClient(accessTokenResponseClient))
                .build();

        var authorizedClientManager = new DefaultReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    private static WebClient webClient(OAuth2ClientPropertiesExt properties) {
        var httpClient = HttpClient.create()
                .option(CONNECT_TIMEOUT_MILLIS, toIntExact(properties.getConnectionTimeout()
                        .orElse(DEFAULT_CONNECTION_TIMEOUT)
                        .toMillis()))
                .responseTimeout(properties.getReadTimeout().orElse(DEFAULT_READ_TIMEOUT));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
