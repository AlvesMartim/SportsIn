package org.SportsIn.repository;

import org.SportsIn.model.strava.StravaActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StravaActivityRepository extends JpaRepository<StravaActivity, Long> {
    List<StravaActivity> findByJoueurIdOrderByStartDateDesc(Long joueurId);
    Optional<StravaActivity> findByJoueurIdAndStravaActivityId(Long joueurId, Long stravaActivityId);
    boolean existsByJoueurIdAndStravaActivityId(Long joueurId, Long stravaActivityId);
}
