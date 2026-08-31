package com.fpl.ultimate.draft.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fpl.ultimate.draft.dto.DraftLeagueDetails;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class FplDraftApiClientImpl implements FplDraftApiClient {

    private final FplDraftApiClientConfig config;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    public FplDraftApiClientImpl(FplDraftApiClientConfig config) {
        this.config = config;
    }

    @Override
    public DraftLeagueDetails getLeagueDetails() {
        Request request = new Request.Builder()
                .url(config.getHost() + "/api/league/" + config.getLeagueId() + "/details")
                .get()
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new FplDraftApiException("FPL Draft API returned status " + response.code());
            }
            return objectMapper.readValue(response.body().string(), DraftLeagueDetails.class);
        } catch (IOException e) {
            log.error("Failed to fetch league details from FPL Draft API", e);
            throw new FplDraftApiException("Failed to fetch league details", e);
        }
    }
}
