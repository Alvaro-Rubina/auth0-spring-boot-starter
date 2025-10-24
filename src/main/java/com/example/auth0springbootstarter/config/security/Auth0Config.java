package com.example.auth0springbootstarter.config.security;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.SimpleTokenProvider;
import com.auth0.exception.Auth0Exception;
import com.auth0.net.client.Auth0HttpClient;
import com.auth0.net.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Auth0Config {

    @Value("${auth0.domain}")
    private  String domain;

    @Value("${auth0.client.id}")
    private  String clientId;

    @Value("${auth0.client.secret}")
    private  String clientSecret;

    @Bean
    public Auth0HttpClient auth0HttpClient() {
        return DefaultHttpClient.newBuilder()
                .withConnectTimeout(10)
                .withReadTimeout(10)
                .build();
    }

    @Bean
    public AuthAPI authAPI(Auth0HttpClient httpClient) {
        return AuthAPI.newBuilder(domain, clientId, clientSecret)
                .withHttpClient(httpClient)
                .build();
    }

    @Bean
    public ManagementAPI managementAPI(Auth0HttpClient httpClient, AuthAPI authAPI) throws Auth0Exception {
        // obtengo token inicial
        String apiToken = authAPI.requestToken("https://" + domain + "/api/v2/")
                .execute()
                .getBody()
                .getAccessToken();

        // acá se maneja la renovación automática del token
        return ManagementAPI.newBuilder(domain, SimpleTokenProvider.create(apiToken))
                .withHttpClient(httpClient)
                .build();
    }

}