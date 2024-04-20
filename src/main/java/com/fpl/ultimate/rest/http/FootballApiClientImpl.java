package com.fpl.ultimate.rest.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FootballApiClientImpl implements FootballApiClient {
    private final FootballApiClientConfig config;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-rapidapi-host", config.getHost());
        headers.set("x-rapidapi-key", config.getKey());
        return headers;
    }

    @Autowired
    public FootballApiClientImpl(FootballApiClientConfig footballApiClientConfig) {
        this.config = footballApiClientConfig;
    }

    @Override
    public List<String> getTimezones() {
        return List.of("Prod times!");
    }
}