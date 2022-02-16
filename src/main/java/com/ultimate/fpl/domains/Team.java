package com.ultimate.fpl.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Team {
    @Id
    @Column(name = "id", nullable = false)
    private int id;

    private String fullName;
    private String tvAbbreviation;
    private String abbreviation;
    private String crest;

}
