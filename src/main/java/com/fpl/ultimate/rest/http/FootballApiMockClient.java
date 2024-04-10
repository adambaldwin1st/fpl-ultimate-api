package com.fpl.ultimate.rest.http;

public class FootballApiMockClient implements FootballApiClient {
    @Override
    public String fetchData() {
        return "Mocked football data";
    }

    @Override
    public void sendData(String data) {
        System.out.println("Sending mock football data: " + data);
    }
}
