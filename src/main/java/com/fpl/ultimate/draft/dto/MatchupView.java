package com.fpl.ultimate.draft.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchupView {

    private int gameweek;
    private String homeTeamName;
    private int homeScore;
    private String awayTeamName;
    private int awayScore;
    private boolean finished;
}
