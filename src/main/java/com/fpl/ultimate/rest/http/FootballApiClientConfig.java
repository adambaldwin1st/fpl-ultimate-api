package com.fpl.ultimate.rest.http;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FootballApiClientConfig {

    @Value("${football.api.host}")
    private String host;

    @Value("${football.api.username}")
    private String username;

    @Value("${football.api.password}")
    private String password;
}
