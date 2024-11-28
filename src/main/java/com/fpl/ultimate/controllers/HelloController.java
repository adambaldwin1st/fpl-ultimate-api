package com.fpl.ultimate.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HelloController {

    @Value("${football.api.username}")
    String username;

    @GetMapping(path = "/", produces = "text/plain")
    public String home() {
        return "FPL Ultimate API";
    }

    @GetMapping(path = "/health", produces = "text/plain")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FPL Ultimate API is up and running!");
    }
}
