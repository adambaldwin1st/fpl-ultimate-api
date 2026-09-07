package com.fpl.ultimate.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class WireMockLocalConfig {

    @Value("${wiremock.port:8089}")
    private int port;

    @Bean(destroyMethod = "stop")
    public WireMockServer wireMockServer() {
        WireMockServer server = new WireMockServer(
                WireMockConfiguration.options()
                        .port(port)
                        .usingFilesUnderDirectory("wiremock")
        );
        server.start();
        return server;
    }
}
