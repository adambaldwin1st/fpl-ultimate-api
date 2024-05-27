package com.fpl.ultimate.repositories;

import com.fpl.ultimate.models.FplManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FplManagerRepository extends JpaRepository<FplManager, Long> {
    // You can define custom query methods here if needed
}
