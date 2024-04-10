package com.fpl.ultimate.models;

import com.fpl.ultimate.constants.FieldPosition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "preferred_name", length = 50)
    private String preferredName;

    @Column(name = "birthday")
    private OffsetDateTime birthday;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(name = "primary_position", nullable = false)
    @Enumerated(EnumType.STRING)
    private FieldPosition primaryPosition;

    @ElementCollection
    @CollectionTable(name = "player_positions", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "position")
    @Enumerated(EnumType.STRING)
    private List<FieldPosition> positions;

}

