package com.fpl.ultimate.draft.client;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FplDraftApiClientConfig {

    @Value("${fpl.draft.api.host}")
    private String host;

    @Value("${fpl.draft.league.id}")
    private long leagueId;
}
