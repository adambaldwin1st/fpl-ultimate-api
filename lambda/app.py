"""
Short-term FPL Draft scraper, deployed as a standalone AWS Lambda with a Function URL.

Mirrors the logic in the Java DraftLeagueService (src/main/java/com/fpl/ultimate/draft/)
so behavior stays consistent with the long-term Spring Boot API. This module has no
dependency on the Java code or the Maven build — it's plain Python, deployed separately.
"""

import json
import os
import urllib.request
import urllib.error

FPL_DRAFT_HOST = os.environ.get("FPL_DRAFT_API_HOST", "https://draft.premierleague.com")
LEAGUE_ID = os.environ.get("FPL_DRAFT_LEAGUE_ID")


class FplDraftApiError(Exception):
    pass


def fetch_league_details():
    if not LEAGUE_ID:
        raise FplDraftApiError("FPL_DRAFT_LEAGUE_ID environment variable is not set")

    url = f"{FPL_DRAFT_HOST}/api/league/{LEAGUE_ID}/details"
    try:
        with urllib.request.urlopen(url, timeout=10) as response:
            return json.loads(response.read())
    except urllib.error.HTTPError as e:
        raise FplDraftApiError(f"FPL Draft API returned status {e.code}") from e
    except urllib.error.URLError as e:
        raise FplDraftApiError(f"Failed to reach FPL Draft API: {e.reason}") from e


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
        "team_name": entry["entry_name"],
        "manager_name": f'{entry["player_first_name"]} {entry["player_last_name"]}',
        "played": played_by_entry.get(standing["league_entry"], 0),
        "won": standing["matches_won"],
        "drawn": standing["matches_drawn"],
        "lost": standing["matches_lost"],
        "points_for": standing["points_for"],
        "points_against": standing["points_against"],
        "league_points": standing["total"],
    }


def to_matchup_view(match, entries_by_id):
    home = entries_by_id[match["league_entry_1"]]
    away = entries_by_id[match["league_entry_2"]]
    return {
        "gameweek": match["event"],
        "home_team_name": home["entry_name"],
        "home_score": match["league_entry_1_points"],
        "away_team_name": away["entry_name"],
        "away_score": match["league_entry_2_points"],
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


def _response(status_code, body):
    return {
        "statusCode": status_code,
        "headers": {"Content-Type": "application/json"},
        "body": json.dumps(body),
    }


def handler(event, context):
    path = event.get("rawPath") or event.get("path") or "/"

    try:
        if path.rstrip("/").endswith("/standings"):
            return _response(200, get_standings())
        if path.rstrip("/").endswith("/current-matchups"):
            return _response(200, get_current_matchups())
        return _response(404, {"error": f"No route for path '{path}'"})
    except FplDraftApiError as e:
        return _response(502, {"error": str(e)})
    except (KeyError, TypeError) as e:
        return _response(500, {"error": f"Unexpected FPL API response shape: {e}"})
