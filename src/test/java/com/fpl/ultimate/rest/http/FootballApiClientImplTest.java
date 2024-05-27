package com.fpl.ultimate.rest.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ConditionalOnProperty(value = "mock.football.api", havingValue = "false")
public class FootballApiClientImplTest {

    @Mock
    private FootballApiClientConfig config;

    @InjectMocks
    private FootballApiClientImpl footballApiClient;

    @BeforeEach
    public void setUp() {
        when(config.getHost()).thenReturn("mocked-host");
        when(config.getKey()).thenReturn("mocked-key");
    }

    @Test
    @Disabled
    public void testGetTimezones() {
        assertDoesNotThrow(() -> footballApiClient.getTimezones());
    }
}
