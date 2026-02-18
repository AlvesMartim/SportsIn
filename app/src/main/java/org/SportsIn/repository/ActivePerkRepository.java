package org.SportsIn.repository;

import org.SportsIn.model.progression.ActivePerk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivePerkRepository extends JpaRepository<ActivePerk, Long> {

    List<ActivePerk> findByTeamId(Long teamId);

    List<ActivePerk> findByTeamIdAndPerkDefinitionId(Long teamId, Long perkDefinitionId);

    @Query("SELECT ap FROM ActivePerk ap WHERE ap.expiresAt < :now")
    List<ActivePerk> findExpiredBefore(@Param("now") String now);

    @Query("SELECT ap FROM ActivePerk ap WHERE ap.targetId = :targetId AND ap.expiresAt > :now")
    List<ActivePerk> findActiveOnTarget(@Param("targetId") String targetId, @Param("now") String now);

    @Query("SELECT ap FROM ActivePerk ap WHERE ap.teamId = :teamId AND ap.expiresAt > :now")
    List<ActivePerk> findActiveByTeam(@Param("teamId") Long teamId, @Param("now") String now);
}
