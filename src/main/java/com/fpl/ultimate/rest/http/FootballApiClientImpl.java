package com.fpl.ultimate.rest.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
@Slf4j
public class FootballApiClientImpl implements FootballApiClient {
    private final FootballApiClientConfig config;

    private OkHttpClient okHttpClient;

    @Autowired
    public FootballApiClientImpl(FootballApiClientConfig footballApiClientConfig) {
        this.config = footballApiClientConfig;
        this.okHttpClient = new OkHttpClient();
    }

    private Headers createHeaders() {
        return new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("x-rapidapi-host", config.getHost())
                .add("x-rapidapi-key", config.getKey())
                .build();
    }
    private Request.Builder footballApiRequest() {
        return new Request.Builder()
                .headers(createHeaders());
    }

    @Override
    public ResponseEntity<String> getTimezones() {
        Request request = footballApiRequest()
            .url(config.getHost() + config.getTimeZoneUrl())
            .get()
            .build();

        try {
            Response response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful()) {
                log.info("Successful request");
                return ResponseEntity.ok(response.body().string());
            } else {
                return new ResponseEntity<>(HttpStatus.valueOf(response.code()));
            }
        } catch (IOException e) {
            log.error("Well that didn't work", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}