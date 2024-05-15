package com.fpl.ultimate.rest.http.integration;

import com.fpl.ultimate.rest.http.FootballApiClientConfig;
import com.fpl.ultimate.rest.http.FootballApiClientImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Slf4j
public class FootballApiIntegrationTest {

    @Autowired
    private FootballApiClientImpl footballApiClient;

    @Autowired
    private FootballApiClientConfig config;

    @Test
    @Disabled
    public void testGetTimezones() {
        ResponseEntity<String> timezones = footballApiClient.getTimezones();
        assertNotNull(timezones);
        log.info("Success {}", timezones);
    }
}
