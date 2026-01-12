package org.SportsIn.repository;

import org.SportsIn.model.Arene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreneRepository extends JpaRepository<Arene, String> {
    List<Arene> findByControllingTeamId(Long teamId);
    List<Arene> findBySportsDisponiblesContaining(String sport);
}
