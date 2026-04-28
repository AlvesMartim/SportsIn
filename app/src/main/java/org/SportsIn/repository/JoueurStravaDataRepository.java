package org.SportsIn.repository;

import org.SportsIn.model.strava.JoueurStravaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JoueurStravaDataRepository extends JpaRepository<JoueurStravaData, Long> {
    Optional<JoueurStravaData> findByJoueurId(Long joueurId);
    Optional<JoueurStravaData> findByStravaAthleteId(Long stravaAthleteId);
}
