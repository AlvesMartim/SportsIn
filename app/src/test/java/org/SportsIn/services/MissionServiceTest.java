package org.SportsIn.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.SportsIn.model.*;
import org.SportsIn.model.mission.*;
import org.SportsIn.model.territory.*;
import org.SportsIn.model.user.Equipe;
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
 * Utilise des implémentations in-memory pour les repositories territoire/session
 * et des stubs simples pour MissionRepository et EquipeRepository.
 */
class MissionServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private InMemoryMissionRepository missionRepository;
    private InMemoryEquipeRepository equipeRepository;
    private InMemorySessionRepository sessionRepository;
    private InMemoryPointSportifRepository pointRepository;
    private InMemoryZoneRepository zoneRepository;
    private InMemoryRouteRepository routeRepository;
    private TerritoryService territoryService;

    private MissionGenerationService generationService;
    private MissionEvaluationService evaluationService;

    private Sport football;
    private Sport basket;

    @BeforeEach
    void setUp() {
        missionRepository = new InMemoryMissionRepository();
        equipeRepository = new InMemoryEquipeRepository();
        sessionRepository = new InMemorySessionRepository();
        pointRepository = new InMemoryPointSportifRepository();
        zoneRepository = new InMemoryZoneRepository();
        routeRepository = new InMemoryRouteRepository();
        territoryService = new TerritoryService(pointRepository, zoneRepository, routeRepository);

        generationService = new MissionGenerationService(
                missionRepository, pointRepository, zoneRepository, routeRepository, sessionRepository);
        evaluationService = new MissionEvaluationService(
                missionRepository, equipeRepository, pointRepository, routeRepository, sessionRepository, territoryService);

        football = new Sport(1L, "FOOT", "Football", 1L, 1L);
        basket = new Sport(2L, "BASKET", "Basketball", 2L, 2L);

        // Créer 2 équipes
        Equipe equipeA = new Equipe("Équipe Alpha");
        equipeA.setId(1L);
        equipeRepository.save(equipeA);

        Equipe equipeB = new Equipe("Équipe Beta");
        equipeB.setId(2L);
        equipeRepository.save(equipeB);
    }

    // ========================
    // GENERATION TESTS
    // ========================

    @Test
    @DisplayName("generateForTeam ne dépasse pas 3 missions actives")
    void testGenerateDoesNotExceedMaxActive() {
        // Pré-remplir 3 missions actives pour l'équipe 1
        for (int i = 0; i < 3; i++) {
            Mission m = createActiveMission(1L, MissionType.DIVERSITY_SPORT, "Mission " + i);
            m.setPayloadJson("{\"idx\":" + i + "}");
            missionRepository.save(m);
        }

        // Setup: au moins une zone avec des points pour que la génération ait des données
        PointSportif p1 = new PointSportif(1L, "Point A", 48.0, 2.0, List.of(football));
        p1.setControllingTeamId(2L); // contrôlé par adversaire
        pointRepository.save(p1);
        PointSportif p2 = new PointSportif(2L, "Point B", 48.1, 2.1, List.of(football));
        p2.setControllingTeamId(1L); // contrôlé par nous
        pointRepository.save(p2);
        Zone zone = new Zone(1L, "Zone Test", List.of(p1, p2));
        zoneRepository.save(zone);

        // Act
        generationService.generateForTeam(1L);

        // Assert: toujours 3 missions, pas plus
        long activeCount = missionRepository.countActiveByTeam(1L);
        assertEquals(3, activeCount, "Ne doit pas créer de missions au-delà de 3 actives");
    }

    @Test
    @DisplayName("Mission expire quand now > endsAt")
    void testMissionExpires() {
        // Mission qui a expiré il y a 1 heure
        Mission m = createActiveMission(1L, MissionType.RECAPTURE_RECENT_LOSS, "Mission expirée");
        m.setTimestampsFromInstant(
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(5, ChronoUnit.DAYS),
                Instant.now().minus(1, ChronoUnit.HOURS) // expiré
        );
        missionRepository.save(m);

        // Act
        evaluationService.expireActiveMissions();

        // Assert
        Mission updated = missionRepository.findById(m.getId()).orElseThrow();
        assertEquals(MissionStatus.EXPIRED, updated.getStatus());
    }

    @Test
    @DisplayName("evaluateRecapture: SUCCESS quand l'équipe reprend le point")
    void testEvaluateRecaptureSuccess() {
        // Setup: point contrôlé initialement par adversaire
        PointSportif point = new PointSportif(10L, "Point Contesté", 48.0, 2.0, List.of(football));
        point.setControllingTeamId(2L);
        pointRepository.save(point);

        Zone zone = new Zone(1L, "Zone A", List.of(point));
        zoneRepository.save(zone);

        // Créer une mission RECAPTURE pour l'équipe 1
        Mission m = createActiveMission(1L, MissionType.RECAPTURE_RECENT_LOSS, "Reprendre Point Contesté");
        m.setPayloadJson(toJson(Map.of("zoneId", 1L, "pointId", 10L, "windowDays", 7)));
        m.setRewardTeamPoints(50);
        m.setRewardTeamXp(30);
        missionRepository.save(m);

        // Simuler: l'équipe 1 reprend le point
        point.setControllingTeamId(1L);
        pointRepository.save(point);

        // Act
        Mission evaluated = evaluationService.evaluateMission(m.getId());

        // Assert
        assertEquals(MissionStatus.SUCCESS, evaluated.getStatus());
        assertEquals(1, evaluated.getProgressCurrent());
        assertNotNull(evaluated.getCompletedAt());

        // Vérifier que les points ont été attribués
        Equipe equipe = equipeRepository.findById(1L).orElseThrow();
        assertEquals(50, equipe.getPoints());
        assertEquals(30, equipe.getXp());
    }

    @Test
    @DisplayName("evaluateRecapture: reste ACTIVE quand le point n'est pas repris")
    void testEvaluateRecaptureStillActive() {
        PointSportif point = new PointSportif(10L, "Point Contesté", 48.0, 2.0, List.of(football));
        point.setControllingTeamId(2L); // toujours contrôlé par adversaire
        pointRepository.save(point);

        Mission m = createActiveMission(1L, MissionType.RECAPTURE_RECENT_LOSS, "Reprendre Point");
        m.setPayloadJson(toJson(Map.of("zoneId", 1L, "pointId", 10L, "windowDays", 7)));
        missionRepository.save(m);

        Mission evaluated = evaluationService.evaluateMission(m.getId());

        assertEquals(MissionStatus.ACTIVE, evaluated.getStatus());
        assertEquals(0, evaluated.getProgressCurrent());
    }

    @Test
    @DisplayName("evaluateBreakRoute: SUCCESS quand l'équipe prend un point de la route adverse")
    void testEvaluateBreakRouteSuccess() {
        PointSportif p1 = new PointSportif(1L, "P1", 48.0, 2.0, List.of(football));
        p1.setControllingTeamId(2L);
        PointSportif p2 = new PointSportif(2L, "P2", 48.1, 2.1, List.of(football));
        p2.setControllingTeamId(2L);
        pointRepository.save(p1);
        pointRepository.save(p2);

        Route route = new Route(1L, "Route Alpha", "Route test", List.of(p1, p2));
        routeRepository.save(route);

        Mission m = createActiveMission(1L, MissionType.BREAK_ROUTE, "Briser Route Alpha");
        m.setPayloadJson(toJson(Map.of("routeId", 1L, "adversaryTeamId", 2L, "minCount", 1)));
        m.setRewardTeamPoints(75);
        missionRepository.save(m);

        // Équipe 1 prend un point de la route
        p1.setControllingTeamId(1L);
        pointRepository.save(p1);

        Mission evaluated = evaluationService.evaluateMission(m.getId());

        assertEquals(MissionStatus.SUCCESS, evaluated.getStatus());
        assertEquals(1, evaluated.getProgressCurrent());
    }

    @Test
    @DisplayName("La génération crée des missions RECAPTURE quand un point adverse est dans une zone partagée")
    void testGenerateRecaptureMission() {
        PointSportif p1 = new PointSportif(1L, "Notre Point", 48.0, 2.0, List.of(football));
        p1.setControllingTeamId(1L);
        PointSportif p2 = new PointSportif(2L, "Point Perdu", 48.05, 2.05, List.of(football));
        p2.setControllingTeamId(2L);
        pointRepository.save(p1);
        pointRepository.save(p2);

        Zone zone = new Zone(1L, "Zone Mixte", List.of(p1, p2));
        zoneRepository.save(zone);

        generationService.generateForTeam(1L);

        List<Mission> active = missionRepository.findActiveByTeam(1L);
        assertFalse(active.isEmpty(), "Au moins une mission doit être générée");
        assertTrue(active.stream().anyMatch(m -> m.getType() == MissionType.RECAPTURE_RECENT_LOSS));
    }

    // ========================
    // HELPERS
    // ========================

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

    /**
     * Stub in-memory pour MissionRepository (simule JpaRepository).
     */
    static class InMemoryMissionRepository implements MissionRepository {
        private final Map<Long, Mission> db = new LinkedHashMap<>();
        private long nextId = 1;

        @Override
        public List<Mission> findByTeamIdAndStatus(Long teamId, MissionStatus status) {
            return db.values().stream()
                    .filter(m -> m.getTeamId().equals(teamId) && m.getStatus() == status)
                    .toList();
        }

        @Override
        public List<Mission> findActiveByTeam(Long teamId) {
            return db.values().stream()
                    .filter(m -> m.getTeamId().equals(teamId) && m.getStatus() == MissionStatus.ACTIVE)
                    .sorted(Comparator.comparing(Mission::getEndsAt))
                    .toList();
        }

        @Override
        public List<Mission> findActiveEndingBefore(String now) {
            return db.values().stream()
                    .filter(m -> m.getStatus() == MissionStatus.ACTIVE && m.getEndsAt().compareTo(now) < 0)
                    .toList();
        }

        @Override
        public long countActiveByTeam(Long teamId) {
            return db.values().stream()
                    .filter(m -> m.getTeamId().equals(teamId) && m.getStatus() == MissionStatus.ACTIVE)
                    .count();
        }

        @Override
        public List<Mission> findAllActive() {
            return db.values().stream()
                    .filter(m -> m.getStatus() == MissionStatus.ACTIVE)
                    .toList();
        }

        @Override
        public List<Mission> findByTeamIdOrderByEndsAtAsc(Long teamId) {
            return db.values().stream()
                    .filter(m -> m.getTeamId().equals(teamId))
                    .sorted(Comparator.comparing(Mission::getEndsAt))
                    .toList();
        }

        @Override
        public <S extends Mission> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(nextId++);
            }
            db.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<Mission> findById(Long id) {
            return Optional.ofNullable(db.get(id));
        }

        @Override
        public boolean existsById(Long id) {
            return db.containsKey(id);
        }

        @Override public <S extends Mission> List<S> saveAll(Iterable<S> entities) {
            List<S> result = new ArrayList<>();
            entities.forEach(e -> result.add(save(e)));
            return result;
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

    /**
     * Stub in-memory pour EquipeRepository.
     */
    static class InMemoryEquipeRepository implements EquipeRepository {
        private final Map<Long, Equipe> db = new LinkedHashMap<>();

        @Override
        public Optional<Equipe> findByNom(String nom) {
            return db.values().stream().filter(e -> e.getNom().equals(nom)).findFirst();
        }

        @Override
        public <S extends Equipe> S save(S entity) {
            db.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<Equipe> findById(Long id) {
            return Optional.ofNullable(db.get(id));
        }

        @Override
        public boolean existsById(Long id) {
            return db.containsKey(id);
        }

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

    /**
     * Stub in-memory pour PointSportifRepository (utilise l'interface existante du projet).
     */
    static class InMemoryPointSportifRepository implements PointSportifRepository {
        private final Map<Long, PointSportif> db = new LinkedHashMap<>();

        @Override
        public Optional<PointSportif> findById(Long id) {
            return Optional.ofNullable(db.get(id));
        }

        @Override
        public List<PointSportif> findAll() {
            return new ArrayList<>(db.values());
        }

        @Override
        public void save(PointSportif point) {
            db.put(point.getId(), point);
        }
    }

    static class InMemoryZoneRepository implements ZoneRepository {
        private final Map<Long, Zone> db = new LinkedHashMap<>();

        @Override
        public Optional<Zone> findById(Long id) {
            return Optional.ofNullable(db.get(id));
        }

        @Override
        public List<Zone> findAll() {
            return new ArrayList<>(db.values());
        }

        @Override
        public void save(Zone zone) {
            db.put(zone.getId(), zone);
        }

        @Override
        public List<Zone> findZonesByPointId(Long pointId) {
            return db.values().stream()
                    .filter(z -> z.getPoints().stream().anyMatch(p -> p.getId().equals(pointId)))
                    .toList();
        }
    }

    static class InMemoryRouteRepository implements RouteRepository {
        private final Map<Long, Route> db = new LinkedHashMap<>();

        @Override
        public Optional<Route> findById(Long id) {
            return Optional.ofNullable(db.get(id));
        }

        @Override
        public List<Route> findAll() {
            return new ArrayList<>(db.values());
        }

        @Override
        public void save(Route route) {
            db.put(route.getId(), route);
        }

        @Override
        public void saveAll(List<Route> routes) {
            routes.forEach(this::save);
        }

        @Override
        public void deleteAll() {
            db.clear();
        }
    }
}
