package com.fpl.ultimate.draft.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DraftLeagueDetails {

    private LeagueInfo league;
    private List<LeagueEntry> leagueEntries;
    private List<Match> matches;
    private List<Standing> standings;
}
