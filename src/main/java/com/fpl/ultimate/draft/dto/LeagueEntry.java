package com.fpl.ultimate.draft.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * "id" is the identifier referenced by Match and Standing entries.
 * "entryId" is a different id (the underlying FPL entry) and is not used for joins here.
 */
@Data
@NoArgsConstructor
public class LeagueEntry {

    private long id;
    private long entryId;
    private String entryName;
    private String playerFirstName;
    private String playerLastName;
    private String shortName;
}
