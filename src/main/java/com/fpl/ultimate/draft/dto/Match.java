package com.fpl.ultimate.draft.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Match {

    private int event;
    private boolean started;
    private boolean finished;
    private long leagueEntry1;
    private int leagueEntry1Points;
    private long leagueEntry2;
    private int leagueEntry2Points;
}
