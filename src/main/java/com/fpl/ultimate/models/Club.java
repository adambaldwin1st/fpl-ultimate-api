package com.fpl.ultimate.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "club")
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "long_name", nullable = false)
    private String longName;

    @Column(name = "abbreviation", nullable = false, length = 4)
    private String abbreviation;

    @Column(name = "home_stadium", nullable = false, length = 100)
    private String homeStadium;

    @Column(name = "crest_url", nullable = false)
    private String crestUrl;

    @Column(name = "nickname", length = 50)
    private String nickname;
}

