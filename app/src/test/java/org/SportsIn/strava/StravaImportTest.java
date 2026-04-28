package org.SportsIn.strava;

import org.SportsIn.model.strava.StravaActivity;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.model.user.Joueur;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.JoueurRepository;
import org.SportsIn.repository.StravaActivityRepository;
import org.SportsIn.services.XpGrantService;
import org.SportsIn.services.strava.StravaActivityService;
import org.SportsIn.services.strava.StravaSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste la logique d'import Strava sans Spring context.
 */
class StravaImportTest {

    private StravaSessionService sessionService;
    private StubStravaActivityRepository activityRepository;
    private StubJoueurRepository joueurRepository;
    private StubEquipeRepository equipeRepository;

    private Equipe equipe;
    private Joueur joueur;

    @BeforeEach
    void setUp() {
        activityRepository = new StubStravaActivityRepository();
        joueurRepository = new StubJoueurRepository();
        equipeRepository = new StubEquipeRepository();
        XpGrantService xpGrantService = new XpGrantService(null, null, null);

        // On passe null pour StravaActivityService car on teste applyImport directement
        sessionService = new StravaSessionService(
            activityRepository, null, joueurRepository, equipeRepository, xpGrantService
        );

        equipe = new Equipe("Les Testeurs");
        equipe.setId(1L);
        equipe.setPoints(0);
        equipeRepository.save(equipe);

        joueur = new Joueur("TestUser", "test@test.com", "password");
        joueur.setId(42L);
        joueur.setEquipe(equipe);
        joueurRepository.save(joueur);
    }

    @Test
    @DisplayName("Import nominal : activité comptabilisée et points attribués")
    void testImportActivity_success() {
        StravaActivity activity = buildActivity(1001L, 5000.0, 1800L, 50.0, false);
        activityRepository.save(activity);

        StravaActivity result = sessionService.applyImport(42L, activity);

        assertTrue(result.isImportedAsSession(), "L'activité doit être marquée comme importée");
        assertEquals(5, equipeRepository.findById(1L).get().getPoints(),
            "5 km → 5 points pour l'équipe");
    }

    @Test
    @DisplayName("Double import interdit : même activité importée deux fois lève une exception")
    void testImportActivity_alreadyImported() {
        StravaActivity activity = buildActivity(2002L, 10000.0, 3600L, 200.0, true); // déjà importée
        activityRepository.save(activity);

        assertThrows(IllegalStateException.class, () ->
            sessionService.applyImport(42L, activity),
            "Doit lever IllegalStateException pour un doublon d'import"
        );
    }

    @Test
    @DisplayName("Joueur sans équipe : import réussi mais aucun point attribué")
    void testImportActivity_joueurSansEquipe() {
        joueur.setEquipe(null);
        joueurRepository.save(joueur);

        StravaActivity activity = buildActivity(3003L, 8000.0, 2400L, 0.0, false);
        activityRepository.save(activity);

        StravaActivity result = sessionService.applyImport(42L, activity);

        assertTrue(result.isImportedAsSession());
        assertEquals(0, equipeRepository.findById(1L).get().getPoints(),
            "Aucun point attribué si le joueur n'a pas d'équipe");
    }

    @Test
    @DisplayName("Bonus effort appliqué : 5 km + 30 min + 100 m → 6 points")
    void testImportActivity_bonusEffort() {
        StravaActivity activity = buildActivity(4004L, 5000.0, 1800L, 100.0, false);
        activityRepository.save(activity);

        sessionService.applyImport(42L, activity);

        assertEquals(6, equipeRepository.findById(1L).get().getPoints());
    }

    // --- Helper ---

    private StravaActivity buildActivity(Long stravaId, Double distance, Long time, Double elev, boolean imported) {
        StravaActivity a = new StravaActivity();
        a.setId(stravaId);
        a.setStravaActivityId(stravaId);
        a.setJoueurId(42L);
        a.setStravaType("Run");
        a.setSportCode("RUNNING");
        a.setName("Test Run");
        a.setDistanceMeters(distance);
        a.setMovingTimeSeconds(time);
        a.setElevationGain(elev);
        a.setImportedAsSession(imported);
        a.setCreatedAt("2026-01-01T10:00:00Z");
        return a;
    }

    // --- Stubs in-memory ---

    static class StubStravaActivityRepository implements StravaActivityRepository {
        private final Map<Long, StravaActivity> db = new LinkedHashMap<>();
        private long nextId = 1L;

        @Override public Optional<StravaActivity> findByJoueurIdAndStravaActivityId(Long joueurId, Long stravaId) {
            return db.values().stream()
                .filter(a -> a.getJoueurId().equals(joueurId) && a.getStravaActivityId().equals(stravaId))
                .findFirst();
        }
        @Override public boolean existsByJoueurIdAndStravaActivityId(Long joueurId, Long stravaId) {
            return findByJoueurIdAndStravaActivityId(joueurId, stravaId).isPresent();
        }
        @Override public List<StravaActivity> findByJoueurIdOrderByStartDateDesc(Long joueurId) {
            return db.values().stream().filter(a -> a.getJoueurId().equals(joueurId)).toList();
        }
        @Override public <S extends StravaActivity> S save(S e) {
            if (e.getId() == null) e.setId(nextId++);
            db.put(e.getId(), e); return e;
        }
        @Override public Optional<StravaActivity> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<StravaActivity> findAll() { return new ArrayList<>(db.values()); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(StravaActivity e) { db.remove(e.getId()); }
        @Override public long count() { return db.size(); }
        @Override public <S extends StravaActivity> List<S> saveAll(Iterable<S> es) { es.forEach(this::save); return List.of(); }
        @Override public List<StravaActivity> findAllById(Iterable<Long> ids) { return List.of(); }
        @Override public void deleteAll() { db.clear(); }
        @Override public void deleteAll(Iterable<? extends StravaActivity> es) {}
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void flush() {}
        @Override public <S extends StravaActivity> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends StravaActivity> List<S> saveAllAndFlush(Iterable<S> es) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<StravaActivity> es) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public StravaActivity getOne(Long id) { return db.get(id); }
        @Override public StravaActivity getById(Long id) { return db.get(id); }
        @Override public StravaActivity getReferenceById(Long id) { return db.get(id); }
        @Override public <S extends StravaActivity> Optional<S> findOne(org.springframework.data.domain.Example<S> ex) { return Optional.empty(); }
        @Override public <S extends StravaActivity> List<S> findAll(org.springframework.data.domain.Example<S> ex) { return List.of(); }
        @Override public <S extends StravaActivity> List<S> findAll(org.springframework.data.domain.Example<S> ex, org.springframework.data.domain.Sort s) { return List.of(); }
        @Override public <S extends StravaActivity> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> ex, org.springframework.data.domain.Pageable p) { return org.springframework.data.domain.Page.empty(); }
        @Override public <S extends StravaActivity> long count(org.springframework.data.domain.Example<S> ex) { return 0; }
        @Override public <S extends StravaActivity> boolean exists(org.springframework.data.domain.Example<S> ex) { return false; }
        @Override public <S extends StravaActivity, R> R findBy(org.springframework.data.domain.Example<S> ex, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
        @Override public List<StravaActivity> findAll(org.springframework.data.domain.Sort s) { return findAll(); }
        @Override public org.springframework.data.domain.Page<StravaActivity> findAll(org.springframework.data.domain.Pageable p) { return org.springframework.data.domain.Page.empty(); }
    }

    static class StubJoueurRepository implements JoueurRepository {
        private final Map<Long, Joueur> db = new LinkedHashMap<>();
        @Override public Optional<Joueur> findByPseudo(String p) { return db.values().stream().filter(j -> p.equals(j.getPseudo())).findFirst(); }
        @Override public Optional<Joueur> findByEmail(String e) { return db.values().stream().filter(j -> e.equals(j.getEmail())).findFirst(); }
        @Override public boolean existsByEmail(String e) { return findByEmail(e).isPresent(); }
        @Override public boolean existsByPseudo(String p) { return findByPseudo(p).isPresent(); }
        @Override public List<Joueur> findByEquipeId(Long id) { return db.values().stream().filter(j -> j.getEquipe() != null && id.equals(j.getEquipe().getId())).toList(); }
        @Override public long countByEquipeId(Long id) { return findByEquipeId(id).size(); }
        @Override public <S extends Joueur> S save(S e) { db.put(e.getId(), e); return e; }
        @Override public Optional<Joueur> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<Joueur> findAll() { return new ArrayList<>(db.values()); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(Joueur e) { db.remove(e.getId()); }
        @Override public long count() { return db.size(); }
        @Override public <S extends Joueur> List<S> saveAll(Iterable<S> es) { es.forEach(this::save); return List.of(); }
        @Override public List<Joueur> findAllById(Iterable<Long> ids) { return List.of(); }
        @Override public void deleteAll() { db.clear(); }
        @Override public void deleteAll(Iterable<? extends Joueur> es) {}
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void flush() {}
        @Override public <S extends Joueur> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends Joueur> List<S> saveAllAndFlush(Iterable<S> es) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<Joueur> es) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public Joueur getOne(Long id) { return db.get(id); }
        @Override public Joueur getById(Long id) { return db.get(id); }
        @Override public Joueur getReferenceById(Long id) { return db.get(id); }
        @Override public <S extends Joueur> Optional<S> findOne(org.springframework.data.domain.Example<S> ex) { return Optional.empty(); }
        @Override public <S extends Joueur> List<S> findAll(org.springframework.data.domain.Example<S> ex) { return List.of(); }
        @Override public <S extends Joueur> List<S> findAll(org.springframework.data.domain.Example<S> ex, org.springframework.data.domain.Sort s) { return List.of(); }
        @Override public <S extends Joueur> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> ex, org.springframework.data.domain.Pageable p) { return org.springframework.data.domain.Page.empty(); }
        @Override public <S extends Joueur> long count(org.springframework.data.domain.Example<S> ex) { return 0; }
        @Override public <S extends Joueur> boolean exists(org.springframework.data.domain.Example<S> ex) { return false; }
        @Override public <S extends Joueur, R> R findBy(org.springframework.data.domain.Example<S> ex, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
        @Override public List<Joueur> findAll(org.springframework.data.domain.Sort s) { return findAll(); }
        @Override public org.springframework.data.domain.Page<Joueur> findAll(org.springframework.data.domain.Pageable p) { return org.springframework.data.domain.Page.empty(); }
    }

    static class StubEquipeRepository implements EquipeRepository {
        private final Map<Long, Equipe> db = new LinkedHashMap<>();
        @Override public Optional<Equipe> findByNom(String n) { return db.values().stream().filter(e -> n.equals(e.getNom())).findFirst(); }
        @Override public <S extends Equipe> S save(S e) { db.put(e.getId(), e); return e; }
        @Override public Optional<Equipe> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<Equipe> findAll() { return new ArrayList<>(db.values()); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(Equipe e) { db.remove(e.getId()); }
        @Override public long count() { return db.size(); }
        @Override public <S extends Equipe> List<S> saveAll(Iterable<S> es) { es.forEach(this::save); return List.of(); }
        @Override public List<Equipe> findAllById(Iterable<Long> ids) { return List.of(); }
        @Override public void deleteAll() { db.clear(); }
        @Override public void deleteAll(Iterable<? extends Equipe> es) {}
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void flush() {}
        @Override public <S extends Equipe> S saveAndFlush(S e) { return save(e); }
        @Override public <S extends Equipe> List<S> saveAllAndFlush(Iterable<S> es) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<Equipe> es) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public Equipe getOne(Long id) { return db.get(id); }
        @Override public Equipe getById(Long id) { return db.get(id); }
        @Override public Equipe getReferenceById(Long id) { return db.get(id); }
        @Override public <S extends Equipe> Optional<S> findOne(org.springframework.data.domain.Example<S> ex) { return Optional.empty(); }
        @Override public <S extends Equipe> List<S> findAll(org.springframework.data.domain.Example<S> ex) { return List.of(); }
        @Override public <S extends Equipe> List<S> findAll(org.springframework.data.domain.Example<S> ex, org.springframework.data.domain.Sort s) { return List.of(); }
        @Override public <S extends Equipe> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> ex, org.springframework.data.domain.Pageable p) { return org.springframework.data.domain.Page.empty(); }
        @Override public <S extends Equipe> long count(org.springframework.data.domain.Example<S> ex) { return 0; }
        @Override public <S extends Equipe> boolean exists(org.springframework.data.domain.Example<S> ex) { return false; }
        @Override public <S extends Equipe, R> R findBy(org.springframework.data.domain.Example<S> ex, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> f) { return null; }
        @Override public List<Equipe> findAll(org.springframework.data.domain.Sort s) { return findAll(); }
        @Override public org.springframework.data.domain.Page<Equipe> findAll(org.springframework.data.domain.Pageable p) { return org.springframework.data.domain.Page.empty(); }
    }
}
