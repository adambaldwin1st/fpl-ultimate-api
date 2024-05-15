package com.fpl.ultimate.rest.http;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FootballApiClient {

    ResponseEntity<String> getTimezones();
}
