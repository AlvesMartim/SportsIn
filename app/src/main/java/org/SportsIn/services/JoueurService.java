package org.SportsIn.services;

import org.SportsIn.model.user.Joueur;
import org.SportsIn.repository.JoueurRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JoueurService {

    private final JoueurRepository joueurRepository;

    public JoueurService(JoueurRepository joueurRepository) {
        this.joueurRepository = joueurRepository;
    }

    public List<Joueur> getAll() {
        return joueurRepository.findAll();
    }

    public Optional<Joueur> getById(Long id) {
        return joueurRepository.findById(id);
    }

    public List<Joueur> getByEquipe(Long equipeId) {
        return joueurRepository.findByEquipeId(equipeId);
    }

    public Joueur create(Joueur joueur) {
        return joueurRepository.save(joueur);
    }

    public Optional<Joueur> update(Long id, Joueur joueurDetails) {
        return joueurRepository.findById(id).map(joueur -> {
            joueur.setPseudo(joueurDetails.getPseudo());
            joueur.setEquipe(joueurDetails.getEquipe());
            return joueurRepository.save(joueur);
        });
    }

    public boolean delete(Long id) {
        if (joueurRepository.existsById(id)) {
            joueurRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
