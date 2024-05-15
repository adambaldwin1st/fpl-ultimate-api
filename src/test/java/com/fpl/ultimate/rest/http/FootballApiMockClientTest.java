package com.fpl.ultimate.rest.http;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class FootballApiMockClientTest {

    @Test
    public void testGetTimezones() {
        FootballApiMockClient mockClient = new FootballApiMockClient();

        ResponseEntity<String> response = mockClient.getTimezones();

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status should be OK");

        String responseBody = response.getBody();
        log.info("Success: {}", responseBody);

        assert responseBody != null && !responseBody.isEmpty() : "Response body should not be empty";
    }
}
