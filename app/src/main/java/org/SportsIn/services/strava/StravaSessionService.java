package org.SportsIn.services.strava;

import org.SportsIn.model.strava.StravaActivity;
import org.SportsIn.model.user.Joueur;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.JoueurRepository;
import org.SportsIn.repository.StravaActivityRepository;
import org.SportsIn.services.XpGrantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Convertit une activité Strava en session SportsIn validée :
 * attribue des points à l'équipe et de l'XP via les services existants.
 */
@Service
public class StravaSessionService {

    private static final Logger log = LoggerFactory.getLogger(StravaSessionService.class);

    private final StravaActivityRepository activityRepository;
    private final StravaActivityService activityService;
    private final JoueurRepository joueurRepository;
    private final EquipeRepository equipeRepository;
    private final XpGrantService xpGrantService;

    public StravaSessionService(StravaActivityRepository activityRepository,
                                StravaActivityService activityService,
                                JoueurRepository joueurRepository,
                                EquipeRepository equipeRepository,
                                XpGrantService xpGrantService) {
        this.activityRepository = activityRepository;
        this.activityService = activityService;
        this.joueurRepository = joueurRepository;
        this.equipeRepository = equipeRepository;
        this.xpGrantService = xpGrantService;
    }

    /**
     * Importe la dernière activité Strava non encore importée du joueur.
     */
    @Transactional
    public StravaActivity importLatestActivity(Long joueurId) {
        List<StravaActivity> fetched = activityService.fetchAndSaveLatestActivities(joueurId);

        // Chercher la première activité non encore importée (ordre start_date desc)
        StravaActivity toImport = fetched.stream()
            .filter(a -> !a.isImportedAsSession())
            .findFirst()
            .orElseGet(() ->
                activityRepository.findByJoueurIdOrderByStartDateDesc(joueurId).stream()
                    .filter(a -> !a.isImportedAsSession())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                        "Aucune nouvelle activité Strava à importer"))
            );

        return applyImport(joueurId, toImport);
    }

    /**
     * Importe une activité Strava précise par son ID.
     */
    @Transactional
    public StravaActivity importActivityById(Long joueurId, Long stravaActivityId) {
        StravaActivity activity = activityRepository
            .findByJoueurIdAndStravaActivityId(joueurId, stravaActivityId)
            .orElseGet(() -> activityService.fetchAndSaveActivityById(joueurId, stravaActivityId));

        return applyImport(joueurId, activity);
    }

    /**
     * Applique l'import : vérifie le doublon, calcule les points, récompense l'équipe.
     */
    public StravaActivity applyImport(Long joueurId, StravaActivity activity) {
        if (activity.isImportedAsSession()) {
            throw new IllegalStateException(
                "L'activité Strava " + activity.getStravaActivityId() + " a déjà été importée");
        }

        int points = StravaPointCalculator.calculate(
            activity.getDistanceMeters(),
            activity.getMovingTimeSeconds(),
            activity.getElevationGain()
        );

        Joueur joueur = joueurRepository.findById(joueurId)
            .orElseThrow(() -> new IllegalArgumentException("Joueur introuvable: " + joueurId));

        if (joueur.getEquipe() != null) {
            Long equipeId = joueur.getEquipe().getId();
            equipeRepository.findById(equipeId).ifPresent(equipe -> {
                equipe.setPoints(equipe.getPoints() + points);
                equipeRepository.save(equipe);
                log.info("Activité Strava {} : +{} points pour l'équipe {} (joueur {})",
                    activity.getStravaActivityId(), points, equipeId, joueurId);
            });
            // XP via le service existant (avec multiplicateurs de perks)
            xpGrantService.grantMatchXp(equipeId, true);
        } else {
            log.warn("Joueur {} n'appartient à aucune équipe, points non attribués", joueurId);
        }

        activity.setImportedAsSession(true);
        return activityRepository.save(activity);
    }
}
