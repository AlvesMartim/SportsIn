package org.SportsIn.repository;

import org.SportsIn.model.strava.StravaActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StravaActivityRepository extends JpaRepository<StravaActivity, Long> {

    Optional<StravaActivity> findByStravaActivityId(String stravaActivityId);

    boolean existsByStravaActivityId(String stravaActivityId);

    List<StravaActivity> findByJoueurIdOrderByStartDateDesc(Long joueurId);

    List<StravaActivity> findByEquipeIdOrderByStartDateDesc(Long equipeId);

    /** Activités non flaggées d'une équipe depuis une date ISO (pour calcul d'influence). */
    @Query("SELECT a FROM StravaActivity a WHERE a.equipeId = :equipeId " +
           "AND a.antiCheatFlagged = false " +
           "AND a.startDate >= :sinceDate " +
           "ORDER BY a.startDate DESC")
    List<StravaActivity> findValidByEquipeSince(@Param("equipeId") Long equipeId,
                                                @Param("sinceDate") String sinceDate);

    /** Activités d'un joueur non encore flaggées, triées par date. */
    List<StravaActivity> findByJoueurIdAndAntiCheatFlaggedFalseOrderByStartDateDesc(Long joueurId);

    /** Stats agrégées : distance totale par équipe. */
    @Query("SELECT SUM(a.distanceMeters) FROM StravaActivity a " +
           "WHERE a.equipeId = :equipeId AND a.antiCheatFlagged = false")
    Double sumDistanceByEquipe(@Param("equipeId") Long equipeId);

    @Query("SELECT SUM(a.totalElevationGain) FROM StravaActivity a " +
           "WHERE a.equipeId = :equipeId AND a.antiCheatFlagged = false")
    Double sumElevationByEquipe(@Param("equipeId") Long equipeId);

    @Query("SELECT COUNT(a) FROM StravaActivity a " +
           "WHERE a.equipeId = :equipeId AND a.antiCheatFlagged = false")
    Long countByEquipe(@Param("equipeId") Long equipeId);

    /**
     * Classement global des équipes par distance totale depuis sinceDate.
     * Retourne [equipeId, totalDistance, activitiesCount].
     */
    @Query("SELECT a.equipeId, SUM(a.distanceMeters), COUNT(a) FROM StravaActivity a " +
           "WHERE a.equipeId IS NOT NULL " +
           "AND a.antiCheatFlagged = false " +
           "AND a.startDate >= :sinceDate " +
           "GROUP BY a.equipeId ORDER BY SUM(a.distanceMeters) DESC")
    List<Object[]> teamLeaderboard(@Param("sinceDate") String sinceDate);

    /**
     * Classement par sport : [equipeId, sportType, totalDistance, count].
     * Permet d'afficher la décomposition Marche / Course / Vélo par équipe.
     */
    @Query("SELECT a.equipeId, a.sportType, SUM(a.distanceMeters), COUNT(a) FROM StravaActivity a " +
           "WHERE a.equipeId IS NOT NULL " +
           "AND a.antiCheatFlagged = false " +
           "AND a.startDate >= :sinceDate " +
           "GROUP BY a.equipeId, a.sportType ORDER BY a.equipeId, SUM(a.distanceMeters) DESC")
    List<Object[]> teamLeaderboardBySport(@Param("sinceDate") String sinceDate);

    /** Toutes les activités valides depuis une date (pour filtrage zone côté service). */
    @Query("SELECT a FROM StravaActivity a WHERE a.antiCheatFlagged = false AND a.startDate >= :sinceDate")
    List<StravaActivity> findValidSince(@Param("sinceDate") String sinceDate);
}
