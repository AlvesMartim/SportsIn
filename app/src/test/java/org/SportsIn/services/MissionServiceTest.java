package org.SportsIn.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.SportsIn.model.*;
import org.SportsIn.model.mission.*;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.MissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MissionGenerationService et MissionEvaluationService.
 * Utilise des stubs in-memory pour tous les repositories.
 */
class MissionServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private InMemoryMissionRepository missionRepository;
    private InMemoryEquipeRepository equipeRepository;
    private InMemoryAreneRepository areneRepository;
    private InMemorySessionRepository sessionRepository;

    private MissionGenerationService generationService;
    private MissionEvaluationService evaluationService;

    private Equipe equipeA;
    private Equipe equipeB;

    @BeforeEach
    void setUp() {
        missionRepository = new InMemoryMissionRepository();
        equipeRepository = new InMemoryEquipeRepository();
        areneRepository = new InMemoryAreneRepository();
        sessionRepository = new InMemorySessionRepository();

        generationService = new MissionGenerationService(
                missionRepository, areneRepository, sessionRepository);
        evaluationService = new MissionEvaluationService(
                missionRepository, equipeRepository, areneRepository, sessionRepository);

        equipeA = new Equipe("Équipe Alpha");
        equipeA.setId(1L);
        equipeRepository.save(equipeA);

        equipeB = new Equipe("Équipe Beta");
        equipeB.setId(2L);
        equipeRepository.save(equipeB);
    }

    // ========================
    // GENERATION TESTS
    // ========================

    @Test
    @DisplayName("generateForTeam ne dépasse pas 3 missions actives")
    void testGenerateDoesNotExceedMaxActive() {
        for (int i = 0; i < 3; i++) {
            Mission m = createActiveMission(1L, MissionType.DIVERSITY_SPORT, "Mission " + i);
            m.setPayloadJson("{\"idx\":" + i + "}");
            missionRepository.save(m);
        }

        Arene arene = createArene("arena1", "Arène Test", equipeB, List.of("FOOT"));
        areneRepository.save(arene);

        generationService.generateForTeam(1L);

        long activeCount = missionRepository.countActiveByTeam(1L);
        assertEquals(3, activeCount, "Ne doit pas créer de missions au-delà de 3 actives");
    }

    @Test
    @DisplayName("La génération crée une mission RECAPTURE quand une arène adverse existe")
    void testGenerateRecaptureMission() {
        Arene arene = createArene("parc_princes", "Parc des Princes", equipeB, List.of("FOOT"));
        areneRepository.save(arene);

        generationService.generateForTeam(1L);

        List<Mission> active = missionRepository.findActiveByTeam(1L);
        assertFalse(active.isEmpty(), "Au moins une mission doit être générée");
        assertTrue(active.stream().anyMatch(m -> m.getType() == MissionType.RECAPTURE_RECENT_LOSS),
                "Doit générer une mission RECAPTURE_RECENT_LOSS");
    }

    @Test
    @DisplayName("La génération crée une mission DIVERSITY quand un sport non joué existe")
    void testGenerateDiversityMission() {
        Arene arene = createArene("stade1", "Stade Test", null, List.of("BASKET", "FOOT"));
        areneRepository.save(arene);

        generationService.generateForTeam(1L);

        List<Mission> active = missionRepository.findActiveByTeam(1L);
        assertTrue(active.stream().anyMatch(m -> m.getType() == MissionType.DIVERSITY_SPORT),
                "Doit générer une mission DIVERSITY_SPORT");
    }

    @Test
    @DisplayName("La génération crée une mission BREAK_ROUTE quand 2+ arènes adverses existent")
    void testGenerateBreakRouteMissionFromArenas() {
        Arene arene1 = createArene("arena1", "Arène 1", equipeB, List.of("FOOT"));
        Arene arene2 = createArene("arena2", "Arène 2", equipeB, List.of("BASKET"));
        areneRepository.save(arene1);
        areneRepository.save(arene2);

        generationService.generateForTeam(1L);

        List<Mission> active = missionRepository.findActiveByTeam(1L);
        assertTrue(active.stream().anyMatch(m -> m.getType() == MissionType.BREAK_ROUTE),
                "Doit générer une mission BREAK_ROUTE");
    }

    // ========================
    // EVALUATION TESTS
    // ========================

    @Test
    @DisplayName("Mission expire quand now > endsAt")
    void testMissionExpires() {
        Mission m = createActiveMission(1L, MissionType.RECAPTURE_RECENT_LOSS, "Mission expirée");
        m.setTimestampsFromInstant(
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(1, ChronoUnit.HOURS)
        );
        missionRepository.save(m);

        evaluationService.expireActiveMissions();

        Mission updated = missionRepository.findById(m.getId()).orElseThrow();
        assertEquals(MissionStatus.EXPIRED, updated.getStatus());
    }

    @Test
    @DisplayName("evaluateRecapture: SUCCESS quand l'équipe contrôle l'arène")
    void testEvaluateRecaptureSuccess() {
        Arene arene = createArene("parc_princes", "Parc des Princes", equipeA, List.of("FOOT"));
        areneRepository.save(arene);

        Mission m = createActiveMission(1L, MissionType.RECAPTURE_RECENT_LOSS, "Reprendre Parc");
        m.setPayloadJson(toJson(Map.of("arenaId", "parc_princes", "arenaName", "Parc des Princes", "windowDays", 7)));
        m.setRewardTeamPoints(50);
        m.setRewardTeamXp(30);
        missionRepository.save(m);

        Mission evaluated = evaluationService.evaluateMission(m.getId());

        assertEquals(MissionStatus.SUCCESS, evaluated.getStatus());
        assertEquals(1, evaluated.getProgressCurrent());
        assertNotNull(evaluated.getCompletedAt());

        Equipe equipe = equipeRepository.findById(1L).orElseThrow();
        assertEquals(50, equipe.getPoints());
        assertEquals(30, equipe.getXp());
    }

    @Test
    @DisplayName("evaluateRecapture: reste ACTIVE quand l'arène est contrôlée par un adversaire")
    void testEvaluateRecaptureStillActive() {
        Arene arene = createArene("parc_princes", "Parc des Princes", equipeB, List.of("FOOT"));
        areneRepository.save(arene);

        Mission m = createActiveMission(1L, MissionType.RECAPTURE_RECENT_LOSS, "Reprendre Parc");
        m.setPayloadJson(toJson(Map.of("arenaId", "parc_princes", "arenaName", "Parc des Princes", "windowDays", 7)));
        missionRepository.save(m);

        Mission evaluated = evaluationService.evaluateMission(m.getId());

        assertEquals(MissionStatus.ACTIVE, evaluated.getStatus());
        assertEquals(0, evaluated.getProgressCurrent());
    }

    @Test
    @DisplayName("evaluateRecapture: retourne false si arenaId absent du payload")
    void testEvaluateRecaptureNoArenaId() {
        Mission m = createActiveMission(1L, MissionType.RECAPTURE_RECENT_LOSS, "Mission sans arène");
        m.setPayloadJson(toJson(Map.of("windowDays", 7)));
        missionRepository.save(m);

        Mission evaluated = evaluationService.evaluateMission(m.getId());

        assertEquals(MissionStatus.ACTIVE, evaluated.getStatus());
    }

    @Test
    @DisplayName("evaluateBreakRoute: SUCCESS quand l'équipe contrôle l'arène")
    void testEvaluateBreakRouteSuccess() {
        Arene arene = createArene("groupama", "Groupama Stadium", equipeA, List.of("FOOT"));
        areneRepository.save(arene);

        Mission m = createActiveMission(1L, MissionType.BREAK_ROUTE, "Briser Groupama");
        m.setPayloadJson(toJson(Map.of("arenaId", "groupama", "arenaName", "Groupama Stadium", "adversaryTeamId", 2, "minCount", 1)));
        m.setRewardTeamPoints(75);
        missionRepository.save(m);

        Mission evaluated = evaluationService.evaluateMission(m.getId());

        assertEquals(MissionStatus.SUCCESS, evaluated.getStatus());
        assertEquals(1, evaluated.getProgressCurrent());
    }

    @Test
    @DisplayName("evaluateAllActiveMissions évalue toutes les missions actives")
    void testEvaluateAllActiveMissions() {
        Arene arene = createArene("parc_princes", "Parc des Princes", equipeA, List.of("FOOT"));
        areneRepository.save(arene);

        Mission m1 = createActiveMission(1L, MissionType.RECAPTURE_RECENT_LOSS, "Mission 1");
        m1.setPayloadJson(toJson(Map.of("arenaId", "parc_princes", "arenaName", "Parc", "windowDays", 7)));
        m1.setRewardTeamPoints(50);
        m1.setRewardTeamXp(30);
        missionRepository.save(m1);

        evaluationService.evaluateAllActiveMissions();

        Mission evaluated = missionRepository.findById(m1.getId()).orElseThrow();
        assertEquals(MissionStatus.SUCCESS, evaluated.getStatus());
    }

    // ========================
    // HELPERS
    // ========================

    private Arene createArene(String id, String nom, Equipe controllingTeam, List<String> sports) {
        Arene arene = new Arene(id, nom, 48.0, 2.0);
        arene.setControllingTeam(controllingTeam);
        arene.setSportsDisponibles(sports);
        return arene;
    }

    private Mission createActiveMission(Long teamId, MissionType type, String title) {
        Mission m = new Mission();
        m.setTeamId(teamId);
        m.setType(type);
        m.setStatus(MissionStatus.ACTIVE);
        m.setTitle(title);
        m.setPriority(MissionPriority.MEDIUM);
        m.setRewardTeamPoints(10);
        m.setRewardTeamXp(5);
        m.setProgressTarget(1);
        Instant now = Instant.now();
        m.setTimestampsFromInstant(now, now, now.plus(3, ChronoUnit.DAYS));
        return m;
    }

    private String toJson(Map<String, Object> map) {
        try {
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

    // ========================
    // IN-MEMORY STUBS
    // ========================

    static class InMemoryAreneRepository implements AreneRepository {
        private final Map<String, Arene> db = new LinkedHashMap<>();

        @Override public List<Arene> findByControllingTeam_Id(Long teamId) {
            return db.values().stream()
                    .filter(a -> a.getControllingTeam() != null && teamId.equals(a.getControllingTeam().getId()))
                    .toList();
        }
        @Override public List<Arene> findBySportsDisponiblesContaining(String sport) {
            return db.values().stream()
                    .filter(a -> a.getSportsDisponibles() != null && a.getSportsDisponibles().contains(sport))
                    .toList();
        }
        @Override public <S extends Arene> S save(S entity) { db.put(entity.getId(), entity); return entity; }
        @Override public Optional<Arene> findById(String id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(String id) { return db.containsKey(id); }
        @Override public List<Arene> findAll() { return new ArrayList<>(db.values()); }
        @Override public <S extends Arene> List<S> saveAll(Iterable<S> entities) { entities.forEach(this::save); return List.of(); }
        @Override public List<Arene> findAllById(Iterable<String> ids) { return List.of(); }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(String id) { db.remove(id); }
        @Override public void delete(Arene entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends String> ids) {}
        @Override public void deleteAll(Iterable<? extends Arene> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends Arene> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Arene> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<Arene> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<String> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public Arene getOne(String id) { return db.get(id); }
        @Override public Arene getById(String id) { return db.get(id); }
        @Override public Arene getReferenceById(String id) { return db.get(id); }
        @Override public <S extends Arene> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends Arene> List<S> findAll(org.springframework.data.domain.Example<S> example) { return List.of(); }
        @Override public <S extends Arene> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return List.of(); }
        @Override public <S extends Arene> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return org.springframework.data.domain.Page.empty(); }
        @Override public <S extends Arene> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends Arene> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends Arene, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<Arene> findAll(org.springframework.data.domain.Sort sort) { return findAll(); }
        @Override public org.springframework.data.domain.Page<Arene> findAll(org.springframework.data.domain.Pageable pageable) { return org.springframework.data.domain.Page.empty(); }
    }

    static class InMemoryMissionRepository implements MissionRepository {
        private final Map<Long, Mission> db = new LinkedHashMap<>();
        private long nextId = 1;

        @Override public List<Mission> findByTeamIdAndStatus(Long teamId, MissionStatus status) {
            return db.values().stream().filter(m -> m.getTeamId().equals(teamId) && m.getStatus() == status).toList();
        }
        @Override public List<Mission> findActiveByTeam(Long teamId) {
            return db.values().stream()
                    .filter(m -> m.getTeamId().equals(teamId) && m.getStatus() == MissionStatus.ACTIVE)
                    .sorted(Comparator.comparing(Mission::getEndsAt)).toList();
        }
        @Override public List<Mission> findActiveEndingBefore(String now) {
            return db.values().stream()
                    .filter(m -> m.getStatus() == MissionStatus.ACTIVE && m.getEndsAt().compareTo(now) < 0).toList();
        }
        @Override public long countActiveByTeam(Long teamId) {
            return db.values().stream().filter(m -> m.getTeamId().equals(teamId) && m.getStatus() == MissionStatus.ACTIVE).count();
        }
        @Override public List<Mission> findAllActive() {
            return db.values().stream().filter(m -> m.getStatus() == MissionStatus.ACTIVE).toList();
        }
        @Override public List<Mission> findByTeamIdOrderByEndsAtAsc(Long teamId) {
            return db.values().stream().filter(m -> m.getTeamId().equals(teamId))
                    .sorted(Comparator.comparing(Mission::getEndsAt)).toList();
        }
        @Override public <S extends Mission> S save(S entity) {
            if (entity.getId() == null) entity.setId(nextId++);
            db.put(entity.getId(), entity);
            return entity;
        }
        @Override public Optional<Mission> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public <S extends Mission> List<S> saveAll(Iterable<S> entities) {
            List<S> r = new ArrayList<>(); entities.forEach(e -> r.add(save(e))); return r;
        }
        @Override public List<Mission> findAll() { return new ArrayList<>(db.values()); }
        @Override public List<Mission> findAllById(Iterable<Long> ids) { return List.of(); }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(Mission entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void deleteAll(Iterable<? extends Mission> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends Mission> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Mission> List<S> saveAllAndFlush(Iterable<S> entities) { return saveAll(entities); }
        @Override public void deleteAllInBatch(Iterable<Mission> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public Mission getOne(Long id) { return db.get(id); }
        @Override public Mission getById(Long id) { return db.get(id); }
        @Override public Mission getReferenceById(Long id) { return db.get(id); }
        @Override public <S extends Mission> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends Mission> List<S> findAll(org.springframework.data.domain.Example<S> example) { return List.of(); }
        @Override public <S extends Mission> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return List.of(); }
        @Override public <S extends Mission> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return org.springframework.data.domain.Page.empty(); }
        @Override public <S extends Mission> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends Mission> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends Mission, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<Mission> findAll(org.springframework.data.domain.Sort sort) { return findAll(); }
        @Override public org.springframework.data.domain.Page<Mission> findAll(org.springframework.data.domain.Pageable pageable) { return org.springframework.data.domain.Page.empty(); }
    }

    static class InMemoryEquipeRepository implements EquipeRepository {
        private final Map<Long, Equipe> db = new LinkedHashMap<>();

        @Override public Optional<Equipe> findByNom(String nom) {
            return db.values().stream().filter(e -> e.getNom().equals(nom)).findFirst();
        }
        @Override public <S extends Equipe> S save(S entity) { db.put(entity.getId(), entity); return entity; }
        @Override public Optional<Equipe> findById(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public boolean existsById(Long id) { return db.containsKey(id); }
        @Override public List<Equipe> findAll() { return new ArrayList<>(db.values()); }
        @Override public <S extends Equipe> List<S> saveAll(Iterable<S> entities) { entities.forEach(this::save); return List.of(); }
        @Override public List<Equipe> findAllById(Iterable<Long> ids) { return List.of(); }
        @Override public long count() { return db.size(); }
        @Override public void deleteById(Long id) { db.remove(id); }
        @Override public void delete(Equipe entity) { db.remove(entity.getId()); }
        @Override public void deleteAllById(Iterable<? extends Long> ids) {}
        @Override public void deleteAll(Iterable<? extends Equipe> entities) {}
        @Override public void deleteAll() { db.clear(); }
        @Override public void flush() {}
        @Override public <S extends Equipe> S saveAndFlush(S entity) { return save(entity); }
        @Override public <S extends Equipe> List<S> saveAllAndFlush(Iterable<S> entities) { return List.of(); }
        @Override public void deleteAllInBatch(Iterable<Equipe> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
        @Override public void deleteAllInBatch() {}
        @Override public Equipe getOne(Long id) { return db.get(id); }
        @Override public Equipe getById(Long id) { return db.get(id); }
        @Override public Equipe getReferenceById(Long id) { return db.get(id); }
        @Override public <S extends Equipe> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends Equipe> List<S> findAll(org.springframework.data.domain.Example<S> example) { return List.of(); }
        @Override public <S extends Equipe> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return List.of(); }
        @Override public <S extends Equipe> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return org.springframework.data.domain.Page.empty(); }
        @Override public <S extends Equipe> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends Equipe> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends Equipe, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public List<Equipe> findAll(org.springframework.data.domain.Sort sort) { return findAll(); }
        @Override public org.springframework.data.domain.Page<Equipe> findAll(org.springframework.data.domain.Pageable pageable) { return org.springframework.data.domain.Page.empty(); }
    }

    static class InMemorySessionRepository implements SessionRepository {
        private final Map<String, Session> db = new LinkedHashMap<>();

        @Override public Session save(Session s) { db.put(s.getId(), s); return s; }
        @Override public Optional<Session> findById(String id) { return Optional.ofNullable(db.get(id)); }
        @Override public List<Session> findByState(SessionState state) {
            return db.values().stream().filter(s -> s.getState() == state).toList();
        }
        @Override public boolean deleteById(String id) { return db.remove(id) != null; }
        @Override public boolean existsById(String id) { return db.containsKey(id); }
        @Override public List<Session> findAll() { return new ArrayList<>(db.values()); }
    }
}