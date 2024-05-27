package com.fpl.ultimate.rest.http;

import org.springframework.http.ResponseEntity;

public interface FootballApiClient {

    ResponseEntity<String> getTimezones();
}
