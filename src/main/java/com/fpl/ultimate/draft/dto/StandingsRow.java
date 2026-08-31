package com.fpl.ultimate.draft.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StandingsRow {

    private int rank;
    private String teamName;
    private String managerName;
    private int played;
    private int won;
    private int drawn;
    private int lost;
    private int pointsFor;
    private int pointsAgainst;
    private int leaguePoints;
}
