package org.SportsIn.config;

import org.SportsIn.model.Arene;
import org.SportsIn.model.territory.Zone;
import org.SportsIn.model.territory.ZoneRepository;
import org.SportsIn.repository.AreneRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Initialise les 8 zones correspondant aux départements d'Île-de-France.
 * Chaque zone contient les arènes qui lui appartiennent géographiquement.
 *
 * Affectation explicite arène → département (plus fiable qu'un bounding-box approximatif).
 */
@Component
public class IdfZoneInitializer {

    /** Mapping arène_id → code département */
    private static final Map<String, String> ARENE_TO_DEPT = Map.ofEntries(
        // Paris 75
        Map.entry("parc_princes",      "75"),
        Map.entry("accor_arena",       "75"),
        Map.entry("roland_garros",     "75"),
        Map.entry("stade_charlety",    "75"),
        // Hauts-de-Seine 92
        Map.entry("la_defense_arena",  "92"),
        Map.entry("stade_nanterre",    "92"),
        // Seine-Saint-Denis 93
        Map.entry("stade_de_france",   "93"),
        Map.entry("stade_gagny",       "93"),
        // Val-de-Marne 94
        Map.entry("stade_creteil",     "94"),
        Map.entry("piscine_nogent",    "94"),
        // Yvelines 78
        Map.entry("velodrome_national","78"),
        Map.entry("stade_versailles",  "78"),
        // Essonne 91
        Map.entry("arena_91",          "91"),
        Map.entry("stade_corbeil",     "91"),
        // Seine-et-Marne 77
        Map.entry("stade_meaux",       "77"),
        Map.entry("patinoire_lagny",   "77"),
        // Val-d'Oise 95
        Map.entry("stade_pontoise",    "95"),
        Map.entry("stade_argenteuil",  "95")
    );

    private static final List<DeptInfo> DEPARTMENTS = List.of(
        new DeptInfo(1L, "75", "Paris"),
        new DeptInfo(2L, "92", "Hauts-de-Seine"),
        new DeptInfo(3L, "93", "Seine-Saint-Denis"),
        new DeptInfo(4L, "94", "Val-de-Marne"),
        new DeptInfo(5L, "78", "Yvelines"),
        new DeptInfo(6L, "91", "Essonne"),
        new DeptInfo(7L, "77", "Seine-et-Marne"),
        new DeptInfo(8L, "95", "Val-d'Oise")
    );

    private final AreneRepository areneRepository;
    private final ZoneRepository zoneRepository;

    public IdfZoneInitializer(AreneRepository areneRepository, ZoneRepository zoneRepository) {
        this.areneRepository = areneRepository;
        this.zoneRepository = zoneRepository;
    }

    public void initialize() {
        List<Arene> allArenes = areneRepository.findAll();

        for (DeptInfo dept : DEPARTMENTS) {
            List<Arene> deptArenes = allArenes.stream()
                    .filter(a -> dept.code().equals(ARENE_TO_DEPT.get(a.getId())))
                    .toList();

            Zone zone = new Zone(
                    dept.id(),
                    dept.name() + " (" + dept.code() + ")",
                    deptArenes
            );
            zoneRepository.save(zone);

            System.out.printf(">>> Zone IDF : %s (%s) — %d arène(s)%n",
                    dept.name(), dept.code(), deptArenes.size());
        }
    }

    private record DeptInfo(Long id, String code, String name) {}
}
