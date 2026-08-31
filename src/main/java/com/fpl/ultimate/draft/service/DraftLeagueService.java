package com.fpl.ultimate.draft.service;

import com.fpl.ultimate.draft.client.FplDraftApiClient;
import com.fpl.ultimate.draft.dto.DraftLeagueDetails;
import com.fpl.ultimate.draft.dto.LeagueEntry;
import com.fpl.ultimate.draft.dto.Match;
import com.fpl.ultimate.draft.dto.MatchupView;
import com.fpl.ultimate.draft.dto.Standing;
import com.fpl.ultimate.draft.dto.StandingsRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DraftLeagueService {

    private final FplDraftApiClient fplDraftApiClient;

    public List<StandingsRow> getStandings() {
        DraftLeagueDetails details = fplDraftApiClient.getLeagueDetails();
        Map<Long, LeagueEntry> entriesById = indexEntriesById(details.getLeagueEntries());
        Map<Long, Integer> playedByEntry = countFinishedMatchesByEntry(details.getMatches());

        return details.getStandings().stream()
                .sorted(Comparator.comparingInt(Standing::getRankSort))
                .map(standing -> toStandingsRow(standing, entriesById, playedByEntry))
                .collect(Collectors.toList());
    }

    public List<MatchupView> getCurrentGameweekMatchups() {
        DraftLeagueDetails details = fplDraftApiClient.getLeagueDetails();
        Map<Long, LeagueEntry> entriesById = indexEntriesById(details.getLeagueEntries());
        int currentGameweek = resolveCurrentGameweek(details.getMatches());

        return details.getMatches().stream()
                .filter(match -> match.getEvent() == currentGameweek)
                .map(match -> toMatchupView(match, entriesById))
                .collect(Collectors.toList());
    }

    private Map<Long, LeagueEntry> indexEntriesById(List<LeagueEntry> entries) {
        return entries.stream().collect(Collectors.toMap(LeagueEntry::getId, entry -> entry));
    }

    /**
     * The API's own "matches_played" field on Standing is pre-seeded with the full season's
     * fixture count rather than actual completed matches, so we derive it ourselves.
     */
    private Map<Long, Integer> countFinishedMatchesByEntry(List<Match> matches) {
        Map<Long, Integer> playedByEntry = new HashMap<>();
        for (Match match : matches) {
            if (match.isFinished()) {
                playedByEntry.merge(match.getLeagueEntry1(), 1, Integer::sum);
                playedByEntry.merge(match.getLeagueEntry2(), 1, Integer::sum);
            }
        }
        return playedByEntry;
    }

    /**
     * The current gameweek is the earliest event that isn't fully finished yet.
     */
    private int resolveCurrentGameweek(List<Match> matches) {
        Map<Integer, List<Match>> matchesByEvent = matches.stream()
                .collect(Collectors.groupingBy(Match::getEvent));

        return matchesByEvent.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(match -> !match.isFinished()))
                .map(Map.Entry::getKey)
                .min(Integer::compareTo)
                .orElseGet(() -> matchesByEvent.keySet().stream().max(Integer::compareTo).orElse(1));
    }

    private StandingsRow toStandingsRow(Standing standing, Map<Long, LeagueEntry> entriesById,
                                         Map<Long, Integer> playedByEntry) {
        LeagueEntry entry = entriesById.get(standing.getLeagueEntry());
        return new StandingsRow(
                standing.getRank(),
                entry.getEntryName(),
                entry.getPlayerFirstName() + " " + entry.getPlayerLastName(),
                playedByEntry.getOrDefault(standing.getLeagueEntry(), 0),
                standing.getMatchesWon(),
                standing.getMatchesDrawn(),
                standing.getMatchesLost(),
                standing.getPointsFor(),
                standing.getPointsAgainst(),
                standing.getTotal()
        );
    }

    private MatchupView toMatchupView(Match match, Map<Long, LeagueEntry> entriesById) {
        LeagueEntry home = entriesById.get(match.getLeagueEntry1());
        LeagueEntry away = entriesById.get(match.getLeagueEntry2());
        return new MatchupView(
                match.getEvent(),
                home.getEntryName(),
                match.getLeagueEntry1Points(),
                away.getEntryName(),
                match.getLeagueEntry2Points(),
                match.isFinished()
        );
    }
}
