"""
Short-term FPL Draft scraper, deployed as a standalone AWS Lambda behind API Gateway.

Mirrors the logic AND the JSON contract (routes, camelCase keys) of the Java
DraftLeagueService (src/main/java/com/fpl/ultimate/draft/) so that swapping this
Lambda for the Spring Boot API later needs no frontend changes. This module has no
dependency on the Java code or the Maven build — it's plain Python, deployed separately.
"""

import json
import os
import urllib.request
import urllib.error

FPL_DRAFT_HOST = os.environ.get("FPL_DRAFT_API_HOST", "https://draft.premierleague.com")
LEAGUE_ID = os.environ.get("FPL_DRAFT_LEAGUE_ID")


class FplDraftApiError(Exception):
    def __init__(self, message, status_code=None):
        super().__init__(message)
        self.status_code = status_code


def _fetch_json(url):
    try:
        with urllib.request.urlopen(url, timeout=10) as response:
            return json.loads(response.read())
    except urllib.error.HTTPError as e:
        raise FplDraftApiError(f"FPL Draft API returned status {e.code} for {url}", status_code=e.code) from e
    except urllib.error.URLError as e:
        raise FplDraftApiError(f"Failed to reach FPL Draft API: {e.reason}") from e


def fetch_league_details():
    if not LEAGUE_ID:
        raise FplDraftApiError("FPL_DRAFT_LEAGUE_ID environment variable is not set")
    return _fetch_json(f"{FPL_DRAFT_HOST}/api/league/{LEAGUE_ID}/details")


def fetch_bootstrap_static():
    return _fetch_json(f"{FPL_DRAFT_HOST}/api/bootstrap-static")


def fetch_entry_picks(entry_id, gameweek):
    return _fetch_json(f"{FPL_DRAFT_HOST}/api/entry/{entry_id}/event/{gameweek}")


def fetch_entry_picks_or_none(entry_id, gameweek):
    """Draft rosters lock shortly before a gameweek's first kickoff - before that,
    this 404s. Returns None in that case so callers can fall back gracefully."""
    try:
        return fetch_entry_picks(entry_id, gameweek)
    except FplDraftApiError as e:
        if e.status_code == 404:
            return None
        raise


def fetch_event_live(gameweek):
    return _fetch_json(f"{FPL_DRAFT_HOST}/api/event/{gameweek}/live")


def fetch_event_fixtures(gameweek):
    return _fetch_json(f"{FPL_DRAFT_HOST}/api/event/{gameweek}/fixtures")


def index_entries_by_id(league_entries):
    return {entry["id"]: entry for entry in league_entries}


def count_finished_matches_by_entry(matches):
    """
    The API's own "matches_played" field on Standing is pre-seeded with the full
    season's fixture count rather than actual completed matches, so we derive it
    ourselves — same as the Java service does.
    """
    played_by_entry = {}
    for match in matches:
        if match.get("finished"):
            for key in ("league_entry_1", "league_entry_2"):
                entry_id = match[key]
                played_by_entry[entry_id] = played_by_entry.get(entry_id, 0) + 1
    return played_by_entry


def resolve_current_gameweek(matches):
    """The current gameweek is the earliest event that isn't fully finished yet."""
    matches_by_event = {}
    for match in matches:
        matches_by_event.setdefault(match["event"], []).append(match)

    unfinished_events = [
        event for event, event_matches in matches_by_event.items()
        if any(not m.get("finished") for m in event_matches)
    ]
    if unfinished_events:
        return min(unfinished_events)
    return max(matches_by_event.keys()) if matches_by_event else 1


def to_standings_row(standing, entries_by_id, played_by_entry):
    entry = entries_by_id[standing["league_entry"]]
    return {
        "rank": standing["rank"],
        "teamName": entry["entry_name"],
        "managerName": f'{entry["player_first_name"]} {entry["player_last_name"]}',
        "played": played_by_entry.get(standing["league_entry"], 0),
        "won": standing["matches_won"],
        "drawn": standing["matches_drawn"],
        "lost": standing["matches_lost"],
        "pointsFor": standing["points_for"],
        "pointsAgainst": standing["points_against"],
        "leaguePoints": standing["total"],
    }


def to_matchup_view(match, entries_by_id):
    home = entries_by_id[match["league_entry_1"]]
    away = entries_by_id[match["league_entry_2"]]
    return {
        "gameweek": match["event"],
        "homeTeamName": home["entry_name"],
        "homeScore": match["league_entry_1_points"],
        "awayTeamName": away["entry_name"],
        "awayScore": match["league_entry_2_points"],
        "finished": match["finished"],
    }


def get_standings():
    details = fetch_league_details()
    entries_by_id = index_entries_by_id(details["league_entries"])
    played_by_entry = count_finished_matches_by_entry(details["matches"])

    standings = sorted(details["standings"], key=lambda s: s["rank_sort"])
    return [to_standings_row(s, entries_by_id, played_by_entry) for s in standings]


def get_current_matchups():
    details = fetch_league_details()
    entries_by_id = index_entries_by_id(details["league_entries"])
    current_gameweek = resolve_current_gameweek(details["matches"])

    return [
        to_matchup_view(m, entries_by_id)
        for m in details["matches"]
        if m["event"] == current_gameweek
    ]


POSITION_TYPE_NAMES = {1: "GKP", 2: "DEF", 3: "MID", 4: "FWD"}


def build_fixture_lookup(fixtures):
    """Maps a real-world PL team id -> its opponent/venue/status for one gameweek."""
    lookup = {}
    for fixture in fixtures:
        lookup[fixture["team_h"]] = {
            "opponent_team_id": fixture["team_a"],
            "is_home": True,
            "started": fixture["started"],
        }
        lookup[fixture["team_a"]] = {
            "opponent_team_id": fixture["team_h"],
            "is_home": False,
            "started": fixture["started"],
        }
    return lookup


def flatten_breakdown(explain):
    """explain is [[[{name, points, value, stat}, ...], fixtureId], ...] - drop the
    fixture-id wrapper since a player only ever has one fixture in this league."""
    return [
        {"name": stat["name"], "points": stat["points"], "value": stat["value"]}
        for entry in explain
        for stat in entry[0]
    ]


def build_team_gameweek_points(entry, roster_gameweek, elements_by_id, team_short_names, fixture_lookup, live_elements):
    # entry["entry_id"] (the real FPL Draft entry/team id, used to fetch picks) is a
    # DIFFERENT field from entry["id"] (the league-entry id used in matches/standings)
    # - easy to mix up since both are plain ints on the same object.
    #
    # roster_gameweek may be one gameweek behind the real target gameweek (see
    # get_gameweek_points) when that gameweek's lineups aren't locked yet - fixture
    # data is always for the real target gameweek regardless, so every player just
    # naturally shows "opponent (H/A)" since none of those fixtures have started.
    picks = fetch_entry_picks(entry["entry_id"], roster_gameweek)["picks"]

    players = []
    total_points = 0
    for pick in picks:
        element = elements_by_id[pick["element"]]
        team_fixture = fixture_lookup.get(element["team"])
        started = bool(team_fixture and team_fixture["started"])
        live = live_elements.get(str(pick["element"])) if started else None
        points = live["stats"]["total_points"] if live else None

        if pick["position"] <= 11 and points is not None:
            total_points += points

        players.append({
            "name": f'{element["first_name"]} {element["second_name"]}',
            "positionType": POSITION_TYPE_NAMES.get(element["element_type"], "UNK"),
            "squadPosition": pick["position"],
            "isStarter": pick["position"] <= 11,
            "opponent": team_short_names.get(team_fixture["opponent_team_id"], "—") if team_fixture else "—",
            "isHome": team_fixture["is_home"] if team_fixture else None,
            "started": started,
            "points": points,
            "breakdown": flatten_breakdown(live["explain"]) if live else [],
        })

    return {
        "teamName": entry["entry_name"],
        "managerName": f'{entry["player_first_name"]} {entry["player_last_name"]}',
        "totalPoints": total_points,
        "players": players,
    }


def get_gameweek_points(selected_team_name):
    details = fetch_league_details()
    entries_by_id = index_entries_by_id(details["league_entries"])
    entries_by_name = {entry["entry_name"]: entry for entry in details["league_entries"]}

    selected_entry = entries_by_name.get(selected_team_name)
    if not selected_entry:
        raise FplDraftApiError(f"No team named '{selected_team_name}' in this league")

    gameweek = resolve_current_gameweek(details["matches"])
    match = next(
        (m for m in details["matches"]
         if m["event"] == gameweek
         and selected_entry["id"] in (m["league_entry_1"], m["league_entry_2"])),
        None,
    )
    if not match:
        raise FplDraftApiError(f"No gameweek {gameweek} match found for '{selected_team_name}'")

    opponent_id = (
        match["league_entry_2"] if match["league_entry_1"] == selected_entry["id"] else match["league_entry_1"]
    )
    opponent_entry = entries_by_id[opponent_id]

    # See fetch_entry_picks_or_none - falls back to last gameweek's roster, shown
    # against this gameweek's real (not-yet-started) fixtures, when this gameweek's
    # lineups aren't locked yet.
    roster_gameweek = gameweek if fetch_entry_picks_or_none(selected_entry["entry_id"], gameweek) else gameweek - 1

    bootstrap = fetch_bootstrap_static()
    elements_by_id = {el["id"]: el for el in bootstrap["elements"]}
    team_short_names = {team["id"]: team["short_name"] for team in bootstrap["teams"]}
    fixture_lookup = build_fixture_lookup(fetch_event_fixtures(gameweek))
    live_elements = fetch_event_live(gameweek)["elements"]

    def team_payload(entry):
        return build_team_gameweek_points(
            entry, roster_gameweek, elements_by_id, team_short_names, fixture_lookup, live_elements
        )

    return {
        "gameweek": gameweek,
        "selectedTeam": team_payload(selected_entry),
        "opponentTeam": team_payload(opponent_entry),
    }


def _response(status_code, body):
    return {
        "statusCode": status_code,
        "headers": {"Content-Type": "application/json"},
        "body": json.dumps(body),
    }


def handler(event, context):
    path = event.get("rawPath") or event.get("path") or "/"
    query_params = event.get("queryStringParameters") or {}

    try:
        if path.rstrip("/").endswith("/league/standings"):
            return _response(200, get_standings())
        if path.rstrip("/").endswith("/league/current-matchups"):
            return _response(200, get_current_matchups())
        if path.rstrip("/").endswith("/league/gameweek-points"):
            team_name = query_params.get("team")
            if not team_name:
                return _response(400, {"error": "Missing required query parameter 'team'"})
            return _response(200, get_gameweek_points(team_name))
        return _response(404, {"error": f"No route for path '{path}'"})
    except FplDraftApiError as e:
        return _response(502, {"error": str(e)})
    except (KeyError, TypeError) as e:
        return _response(500, {"error": f"Unexpected FPL API response shape: {e}"})
