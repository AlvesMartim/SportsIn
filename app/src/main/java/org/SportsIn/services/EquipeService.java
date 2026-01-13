package org.SportsIn.services;

import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.EquipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipeService {

    private final EquipeRepository equipeRepository;

    public EquipeService(EquipeRepository equipeRepository) {
        this.equipeRepository = equipeRepository;
    }

    public List<Equipe> getAll() {
        return equipeRepository.findAll();
    }

    public Optional<Equipe> getById(Long id) {
        return equipeRepository.findById(id);
    }

    public Equipe create(Equipe equipe) {
        return equipeRepository.save(equipe);
    }

    public Optional<Equipe> update(Long id, Equipe equipeDetails) {
        return equipeRepository.findById(id).map(equipe -> {
            equipe.setNom(equipeDetails.getNom());
            return equipeRepository.save(equipe);
        });
    }

    public boolean delete(Long id) {
        if (equipeRepository.existsById(id)) {
            equipeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
