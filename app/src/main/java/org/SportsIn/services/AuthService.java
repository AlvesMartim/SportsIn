package org.SportsIn.services;

import org.SportsIn.model.user.Joueur;
import org.SportsIn.repository.JoueurRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final JoueurRepository joueurRepository;

    public AuthService(JoueurRepository joueurRepository) {
        this.joueurRepository = joueurRepository;
    }

    /**
     * Inscription d'un nouveau joueur
     */
    public Joueur register(String pseudo, String email, String password) {
        // Vérifier si l'email existe déjà
        if (joueurRepository.existsByEmail(email)) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Vérifier si le pseudo existe déjà
        if (joueurRepository.existsByPseudo(pseudo)) {
            throw new RuntimeException("Ce pseudo est déjà utilisé");
        }

        // Créer le joueur (mot de passe stocké en clair pour la simplicité)
        // Dans une vraie app, utiliser BCrypt pour hasher le mot de passe
        Joueur joueur = new Joueur(pseudo, email, password);
        return joueurRepository.save(joueur);
    }

    /**
     * Connexion d'un joueur
     */
    public Optional<Joueur> login(String email, String password) {
        Optional<Joueur> joueurOpt = joueurRepository.findByEmail(email);

        if (joueurOpt.isPresent()) {
            Joueur joueur = joueurOpt.get();
            // Vérifier le mot de passe (en clair pour la simplicité)
            if (joueur.getPassword().equals(password)) {
                return Optional.of(joueur);
            }
        }

        return Optional.empty();
    }

    /**
     * Récupérer un joueur par ID
     */
    public Optional<Joueur> getById(Long id) {
        return joueurRepository.findById(id);
    }
}
