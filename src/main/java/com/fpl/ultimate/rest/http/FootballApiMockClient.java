package com.fpl.ultimate.rest.http;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
@Profile("local")
public class FootballApiMockClient implements FootballApiClient {
    @Override
    public ResponseEntity<String> getTimezones() {
        try {
            // Load the JSON file from the resources folder
            File file = ResourceUtils.getFile("src/main/java/com/fpl/ultimate/rest/http/mock/responses/timezones.json");
            String content = new String(Files.readAllBytes(file.toPath()));
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            // Handle exception if the file cannot be read
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading JSON file");
        }
    }
}
