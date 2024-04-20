package com.fpl.ultimate.rest.http;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("local")
public class FootballApiMockClient implements FootballApiClient {
    @Override
    public List<String> getTimezones() {
        return List.of("Time");
    }
}
