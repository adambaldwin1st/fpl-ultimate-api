package com.fpl.ultimate.models;

import com.fpl.ultimate.auth.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fpl_manager")
public class FplManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection
    @CollectionTable(name = "manager_squad", joinColumns = @JoinColumn(name = "manager_id"))
    @Column(name = "player_id")
    private List<Long> squad;

    @Column(name = "points", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int points;

    @Column(name = "table_position")
    private Integer tablePosition;

    @Column(name = "nickname", nullable = false, length = 4)
    private String nickname;
}
