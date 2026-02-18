package org.SportsIn.repository;

import org.SportsIn.model.progression.PerkDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerkDefinitionRepository extends JpaRepository<PerkDefinition, Long> {

    Optional<PerkDefinition> findByCode(String code);

    List<PerkDefinition> findByRequiredLevelLessThanEqual(int level);
}
