package com.ultimate.fpl.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Player {
    @Id
    @Column(name = "id", nullable = false)
    private int id;

    private String firstName;
    private String lastName;
    private String commonName;
    @ManyToOne
    @JoinColumn
    private Team team;
    private int position;
    private int number;
    private int goals;
    private int assists;
    @ManyToOne
    @JoinColumn
    private Manager manager;
}
