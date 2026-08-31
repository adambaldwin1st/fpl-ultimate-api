package com.fpl.ultimate.draft.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Standing {

    private long leagueEntry;
    private int rank;
    private int rankSort;
    private int matchesWon;
    private int matchesDrawn;
    private int matchesLost;
    private int pointsFor;
    private int pointsAgainst;
    private int total;
}
