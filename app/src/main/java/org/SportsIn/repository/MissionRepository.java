package org.SportsIn.repository;

import org.SportsIn.model.mission.Mission;
import org.SportsIn.model.mission.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findByTeamIdAndStatus(Long teamId, MissionStatus status);

    @Query("SELECT m FROM Mission m WHERE m.teamId = :teamId AND m.status = 'ACTIVE' ORDER BY m.endsAt ASC")
    List<Mission> findActiveByTeam(@Param("teamId") Long teamId);

    @Query("SELECT m FROM Mission m WHERE m.status = 'ACTIVE' AND m.endsAt < :now")
    List<Mission> findActiveEndingBefore(@Param("now") String now);

    @Query("SELECT COUNT(m) FROM Mission m WHERE m.teamId = :teamId AND m.status = 'ACTIVE'")
    long countActiveByTeam(@Param("teamId") Long teamId);

    @Query("SELECT m FROM Mission m WHERE m.status = 'ACTIVE'")
    List<Mission> findAllActive();

    List<Mission> findByTeamIdOrderByEndsAtAsc(Long teamId);
}
