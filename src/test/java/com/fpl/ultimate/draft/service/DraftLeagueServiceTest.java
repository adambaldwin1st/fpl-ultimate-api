package com.fpl.ultimate.draft.service;

import com.fpl.ultimate.draft.client.FplDraftApiClient;
import com.fpl.ultimate.draft.dto.DraftLeagueDetails;
import com.fpl.ultimate.draft.dto.LeagueEntry;
import com.fpl.ultimate.draft.dto.Match;
import com.fpl.ultimate.draft.dto.MatchupView;
import com.fpl.ultimate.draft.dto.Standing;
import com.fpl.ultimate.draft.dto.StandingsRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftLeagueServiceTest {

    private static final long ENTRY_1_ID = 101L;
    private static final long ENTRY_2_ID = 102L;

    @Mock
    private FplDraftApiClient fplDraftApiClient;

    private DraftLeagueService draftLeagueService;

    @BeforeEach
    void setUp() {
        draftLeagueService = new DraftLeagueService(fplDraftApiClient);
        when(fplDraftApiClient.getLeagueDetails()).thenReturn(buildLeagueDetails());
    }

    @Test
    void getStandings_joinsEntryNamesAndDerivesPlayedFromFinishedMatchesOnly() {
        List<StandingsRow> standings = draftLeagueService.getStandings();

        assertEquals(2, standings.size());

        StandingsRow first = standings.get(0);
        assertEquals("Team One", first.getTeamName());
        assertEquals("Alice A", first.getManagerName());
        assertEquals(1, first.getPlayed(), "Only the finished gameweek-1 match should count as played");
        assertEquals(1, first.getRank());
    }

    @Test
    void getCurrentGameweekMatchups_returnsEarliestUnfinishedEvent() {
        List<MatchupView> matchups = draftLeagueService.getCurrentGameweekMatchups();

        assertEquals(1, matchups.size());
        MatchupView matchup = matchups.get(0);
        assertEquals(2, matchup.getGameweek());
        assertEquals("Team One", matchup.getHomeTeamName());
        assertEquals("Team Two", matchup.getAwayTeamName());
        assertEquals(false, matchup.isFinished());
    }

    private DraftLeagueDetails buildLeagueDetails() {
        LeagueEntry entryOne = new LeagueEntry();
        entryOne.setId(ENTRY_1_ID);
        entryOne.setEntryName("Team One");
        entryOne.setPlayerFirstName("Alice");
        entryOne.setPlayerLastName("A");

        LeagueEntry entryTwo = new LeagueEntry();
        entryTwo.setId(ENTRY_2_ID);
        entryTwo.setEntryName("Team Two");
        entryTwo.setPlayerFirstName("Bob");
        entryTwo.setPlayerLastName("B");

        Match finishedMatch = new Match();
        finishedMatch.setEvent(1);
        finishedMatch.setStarted(true);
        finishedMatch.setFinished(true);
        finishedMatch.setLeagueEntry1(ENTRY_1_ID);
        finishedMatch.setLeagueEntry1Points(56);
        finishedMatch.setLeagueEntry2(ENTRY_2_ID);
        finishedMatch.setLeagueEntry2Points(26);

        Match liveMatch = new Match();
        liveMatch.setEvent(2);
        liveMatch.setStarted(true);
        liveMatch.setFinished(false);
        liveMatch.setLeagueEntry1(ENTRY_1_ID);
        liveMatch.setLeagueEntry1Points(32);
        liveMatch.setLeagueEntry2(ENTRY_2_ID);
        liveMatch.setLeagueEntry2Points(36);

        Match futureMatch = new Match();
        futureMatch.setEvent(3);
        futureMatch.setStarted(false);
        futureMatch.setFinished(false);
        futureMatch.setLeagueEntry1(ENTRY_1_ID);
        futureMatch.setLeagueEntry2(ENTRY_2_ID);

        Standing standingOne = new Standing();
        standingOne.setLeagueEntry(ENTRY_1_ID);
        standingOne.setRank(1);
        standingOne.setRankSort(1);
        standingOne.setMatchesWon(1);
        standingOne.setPointsFor(56);
        standingOne.setPointsAgainst(26);
        standingOne.setTotal(3);

        Standing standingTwo = new Standing();
        standingTwo.setLeagueEntry(ENTRY_2_ID);
        standingTwo.setRank(2);
        standingTwo.setRankSort(2);
        standingTwo.setMatchesLost(1);
        standingTwo.setPointsFor(26);
        standingTwo.setPointsAgainst(56);
        standingTwo.setTotal(0);

        DraftLeagueDetails details = new DraftLeagueDetails();
        details.setLeagueEntries(List.of(entryOne, entryTwo));
        details.setMatches(List.of(finishedMatch, liveMatch, futureMatch));
        details.setStandings(List.of(standingOne, standingTwo));
        return details;
    }
}
