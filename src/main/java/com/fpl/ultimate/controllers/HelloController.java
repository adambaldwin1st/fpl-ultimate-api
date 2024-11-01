package com.fpl.ultimate.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HelloController {

    @GetMapping
    public String home() {
        return "Hello, this is the base endpoint";
    }

    @GetMapping(path = "/health", produces = "plain/text")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FPL Ultimate API is up and running!");
    }
}
