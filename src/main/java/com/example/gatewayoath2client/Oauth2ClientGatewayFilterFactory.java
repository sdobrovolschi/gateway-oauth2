package com.example.gatewayoath2client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class Oauth2ClientGatewayFilterFactory extends AbstractGatewayFilterFactory<Oauth2ClientGatewayFilterFactory.Config> {

    public static final String STATUS_KEY = "clientRegistrationId";

    private static final Authentication ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken("anonymous",
            "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

    @Autowired
    private ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

    public Oauth2ClientGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(STATUS_KEY);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(config.clientRegistrationId)
                    .principal(ANONYMOUS_AUTHENTICATION)
                    .build();

            return authorizedClientManager.authorize(authorizeRequest)
                    .map(client -> client.getAccessToken().getTokenValue())
                    .map(bearerToken -> {
                        var builder = exchange.getRequest().mutate();
                        builder.header(AUTHORIZATION, "Bearer " + bearerToken);
                        var request = builder.build();
                        return exchange.mutate().request(request).build();
                    })
                    .defaultIfEmpty(exchange)
                    .flatMap(chain::filter);
        };
    }



    public static class Config {

        private String clientRegistrationId;

        public String getClientRegistrationId() {
            return clientRegistrationId;
        }

        public void setClientRegistrationId(String clientRegistrationId) {
            this.clientRegistrationId = clientRegistrationId;
        }
    }
}
