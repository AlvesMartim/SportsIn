package org.SportsIn.services;

import org.SportsIn.dto.StravaSyncResultDTO;
import org.SportsIn.model.strava.StravaAccount;
import org.SportsIn.repository.StravaAccountRepository;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Traite les événements webhook Strava de façon asynchrone.
 * Spring Events garantit le découplage entre le controller (qui répond immédiatement à Strava)
 * et le traitement réel (qui peut prendre plusieurs secondes).
 */
@Service
public class StravaWebhookService {

    private final StravaAccountRepository accountRepo;
    private final StravaSyncService syncService;
    private final ApplicationEventPublisher eventPublisher;

    public StravaWebhookService(StravaAccountRepository accountRepo,
                                StravaSyncService syncService,
                                ApplicationEventPublisher eventPublisher) {
        this.accountRepo = accountRepo;
        this.syncService = syncService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publie un StravaWebhookEvent pour traitement asynchrone.
     * Appelé par le contrôleur lors de la réception du webhook.
     */
    public void handleWebhookAsync(String objectType, String aspectType,
                                   long objectId, long ownerId) {
        eventPublisher.publishEvent(new StravaWebhookEvent(this, objectType, aspectType, objectId, ownerId));
    }

    /**
     * Listener asynchrone : traite l'événement sans bloquer la réponse HTTP au webhook Strava.
     * Strava exige une réponse en < 2 secondes ; le traitement réel est donc découplé.
     */
    @Async
    @EventListener
    public void onWebhookEvent(StravaWebhookEvent event) {
        if (!"activity".equals(event.getObjectType())) return;
        if (!"create".equals(event.getAspectType()) && !"update".equals(event.getAspectType())) return;

        String stravaAthleteId = String.valueOf(event.getOwnerId());
        Optional<StravaAccount> accountOpt = accountRepo.findByStravaAthleteId(stravaAthleteId);
        if (accountOpt.isEmpty()) {
            System.out.println("Webhook Strava: athlète " + stravaAthleteId + " non connecté, ignoré.");
            return;
        }

        Long joueurId = accountOpt.get().getJoueurId();
        String activityId = String.valueOf(event.getObjectId());

        try {
            StravaSyncResultDTO result = syncService.syncSingleActivity(joueurId, activityId);
            System.out.printf("Webhook Strava traité: activité %s → %d importée, influence=%.2f%n",
                    activityId, result.getActivitiesImported(), result.getTotalInfluenceGranted());
        } catch (Exception e) {
            System.err.println("Erreur traitement webhook Strava activité " + activityId + ": " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Spring Event interne
    // ----------------------------------------------------------------

    public static class StravaWebhookEvent extends ApplicationEvent {
        private final String objectType;
        private final String aspectType;
        private final long objectId;
        private final long ownerId;

        public StravaWebhookEvent(Object source, String objectType, String aspectType,
                                  long objectId, long ownerId) {
            super(source);
            this.objectType = objectType;
            this.aspectType = aspectType;
            this.objectId = objectId;
            this.ownerId = ownerId;
        }

        public String getObjectType() { return objectType; }
        public String getAspectType() { return aspectType; }
        public long getObjectId() { return objectId; }
        public long getOwnerId() { return ownerId; }
    }
}
