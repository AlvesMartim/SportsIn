package org.SportsIn.services;

import org.SportsIn.model.progression.LevelThreshold;
import org.SportsIn.model.progression.PerkDefinition;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.PerkDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamProgressionService {

    private final EquipeRepository equipeRepository;
    private final PerkDefinitionRepository perkDefinitionRepository;

    public TeamProgressionService(EquipeRepository equipeRepository,
                                  PerkDefinitionRepository perkDefinitionRepository) {
        this.equipeRepository = equipeRepository;
        this.perkDefinitionRepository = perkDefinitionRepository;
    }

    public int getLevel(Equipe equipe) {
        return LevelThreshold.levelForXp(equipe.getXp());
    }

    public int getLevel(Long teamId) {
        Equipe equipe = equipeRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Equipe introuvable: " + teamId));
        return getLevel(equipe);
    }

    public int getXpForNextLevel(Equipe equipe) {
        return LevelThreshold.xpForNextLevel(equipe.getXp());
    }

    public List<PerkDefinition> getUnlockedPerks(Equipe equipe) {
        int level = getLevel(equipe);
        return perkDefinitionRepository.findByRequiredLevelLessThanEqual(level);
    }

    public List<PerkDefinition> getUnlockedPerks(Long teamId) {
        Equipe equipe = equipeRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Equipe introuvable: " + teamId));
        return getUnlockedPerks(equipe);
    }

    public List<PerkDefinition> getAllPerkDefinitions() {
        return perkDefinitionRepository.findAll();
    }
}
