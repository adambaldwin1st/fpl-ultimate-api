package com.fpl.ultimate.rest.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FootballApiClientImpl implements FootballApiClient {
    private final FootballApiClientConfig config;

    @Autowired
    public FootballApiClientImpl(FootballApiClientConfig footballApiClientConfig) {
        this.config = footballApiClientConfig;
    }

    @Override
    public String fetchData() {
        // Implementation to fetch football data from the API using host, username, and password
        return "Football data from " + config.getHost();
    }

    @Override
    public void sendData(String data) {
        // Implementation to send football data to the API using host, username, and password
        System.out.println("Sending football data to " + config.getUsername() + ": " + data);
    }
}