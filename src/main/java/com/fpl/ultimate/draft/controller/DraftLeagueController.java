package com.fpl.ultimate.draft.controller;

import com.fpl.ultimate.draft.dto.MatchupView;
import com.fpl.ultimate.draft.dto.StandingsRow;
import com.fpl.ultimate.draft.service.DraftLeagueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/league")
@RequiredArgsConstructor
public class DraftLeagueController {

    private final DraftLeagueService draftLeagueService;

    @GetMapping("/standings")
    public List<StandingsRow> getStandings() {
        return draftLeagueService.getStandings();
    }

    @GetMapping("/current-matchups")
    public List<MatchupView> getCurrentMatchups() {
        return draftLeagueService.getCurrentGameweekMatchups();
    }
}
