package com.fpl.ultimate.repositories;

import com.fpl.ultimate.models.FplLeague;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FplLeagueRepository extends JpaRepository<FplLeague, Long> {

}
