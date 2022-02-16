package com.ultimate.fpl.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Manager {
    @Id
    @Column(name = "id", nullable = false)
    private int id;

    private String firstName;
    private String lastName;
    private String email;
    private String teamName;
    private Set<Player> team;

}
