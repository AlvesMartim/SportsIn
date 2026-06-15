package org.SportsIn.repository;

import org.SportsIn.model.strava.StravaAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StravaAccountRepository extends JpaRepository<StravaAccount, Long> {

    Optional<StravaAccount> findByJoueurId(Long joueurId);

    Optional<StravaAccount> findByStravaAthleteId(String stravaAthleteId);

    boolean existsByJoueurId(Long joueurId);

    void deleteByJoueurId(Long joueurId);
}
