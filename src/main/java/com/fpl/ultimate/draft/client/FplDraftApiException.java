package com.fpl.ultimate.draft.client;

public class FplDraftApiException extends RuntimeException {

    public FplDraftApiException(String message) {
        super(message);
    }

    public FplDraftApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
