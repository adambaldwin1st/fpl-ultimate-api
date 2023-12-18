package com.ultimate.fpl.controllers;

import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${profile.active}")
    private String profile;

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot! The profile is: " + profile;
    }

}